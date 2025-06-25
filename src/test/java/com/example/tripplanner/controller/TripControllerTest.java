package com.example.tripplanner.controller;


import com.example.tripplanner.exceptions.FileProcessingException;
import com.example.tripplanner.exceptions.ResourceNotFoundException;
import com.example.tripplanner.model.dto.TripRequest;
import com.example.tripplanner.model.dto.TripResponse;
import com.example.tripplanner.service.CsvService;
import com.example.tripplanner.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripController.class)
public class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripService tripService;

    @MockitoBean // Mock the CsvService for file I/O tests
    private CsvService csvService;

    @Autowired
    private ObjectMapper objectMapper;

    private TripRequest validTripRequest;
    private TripResponse createdTripResponse;
    private TripResponse existingTripResponse;
    private TripRequest updatedTripRequest;
    private TripResponse updatedTripResponse;
    private TripResponse existingTripResponse1;
    private TripResponse existingTripResponse2;
    private String sampleCsvContentForImport; // CSV content for import test
    private byte[] sampleCsvBytesForExport;
    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        // Prepare a valid TripRequest DTO for sending in the request body
        validTripRequest = new TripRequest(
                "Winter Getaway",
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 10),
                "Skiing trip to the mountains"
        );

        // Prepare the expected TripResponse DTO that the service would return
        createdTripResponse = new TripResponse(
                2L, // Simulate a generated ID for the new trip
                "Winter Getaway",
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 10),
                "Skiing trip to the mountains"
        );

        existingTripResponse = new TripResponse(
                100L,
                "Existing Trip",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 10),
                "A trip that already exists"
        );

        updatedTripRequest = new TripRequest(
                "Updated Trip Name",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5),
                "Updated description"
        );
        updatedTripResponse = new TripResponse(
                100L,
                "Updated Trip Name",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5),
                "Updated description"
        );

        existingTripResponse1 = new TripResponse(
                101L,
                "Europe Tour",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 20),
                "Exploring European cities"
        );
        existingTripResponse2 = new TripResponse(
                102L,
                "Mountain Hike",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 5),
                "Challenging mountain trails"
        );
        sampleCsvContentForImport = "name,startDate,endDate,description\n" +
                "Imported Trip 1,2026-01-01,2026-01-05,First imported trip\n" +
                "Imported Trip 2,2026-02-10,2026-02-15,Second imported trip\n";
        sampleCsvBytesForExport = "id,name,startDate,endDate,description\n101,Europe Tour,2025-06-01,2025-06-20,Exploring European cities\n102,Mountain Hike,2025-08-01,2025-08-05,Challenging mountain trails\n".getBytes();
    }

    @Test
    @DisplayName("Test Trip Create endpoint")
    void testCreate() throws Exception {
        when(tripService.createTrip(any(TripRequest.class))).thenReturn(createdTripResponse);
        mockMvc.perform(
                post("/trips/createTrip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTripRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdTripResponse.getId())) // Assert ID in JSON response
                .andExpect(jsonPath("$.name").value(createdTripResponse.getName())) // Assert name in JSON response
                .andExpect(jsonPath("$.startDate").value(createdTripResponse.getStartDate().toString())) // Assert start date
                .andExpect(jsonPath("$.endDate").value(createdTripResponse.getEndDate().toString())) // Assert end date
                .andExpect(jsonPath("$.description").value(createdTripResponse.getDescription()));

    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid trip creation")
    void shouldReturnBadRequestForInvalidTripCreation() throws Exception {
        TripRequest invalidTripRequest = new TripRequest(
                "",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 10),
                "Invalid trip"
        );

        mockMvc.perform(post("/trips/createTrip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTripRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Trip Name must not be blank"));
    }

    @Test
    @DisplayName("Should retrieve a trip by ID and return 200 OK")
    void shouldRetrieveTripByIdAndReturnOk() throws Exception {
        when(tripService.getTripById(existingTripResponse.getId()))
                .thenReturn(existingTripResponse);

        mockMvc.perform( MockMvcRequestBuilders.get("/trips/{id}", existingTripResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingTripResponse.getId()))
                .andExpect(jsonPath("$.name").value(existingTripResponse.getName()));
    }

    @Test
    @DisplayName("Should return 404 Not Found when trip ID does not exist")
    void shouldReturnNotFoundWhenTripIdDoesNotExist() throws Exception {
        Long nonExistentId = 999L;
        doThrow(new ResourceNotFoundException("Trip not found with ID: " + nonExistentId))
                .when(tripService).getTripById(nonExistentId);

        mockMvc.perform(MockMvcRequestBuilders.get("/trips/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Should retrieve all trips and return 200 OK")
    void shouldRetrieveAllTripsAndReturnOk() throws Exception {
        List<TripResponse> allTrips = Arrays.asList(existingTripResponse1, existingTripResponse2);
        when(tripService.getAllTrips()).thenReturn(allTrips);

        mockMvc.perform(MockMvcRequestBuilders.get("/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(existingTripResponse1.getId()))
                .andExpect(jsonPath("$[1].id").value(existingTripResponse2.getId()));
    }

    @Test
    @DisplayName("Should return empty array when no trips exist")
    void shouldReturnEmptyArrayWhenNoTripsExist() throws Exception {
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should update an existing trip and return 200 OK")
    void shouldUpdateExistingTripAndReturnOk() throws Exception {
        when(tripService.updateTrip(eq(existingTripResponse.getId()), any(TripRequest.class)))
                .thenReturn(updatedTripResponse);

        mockMvc.perform(put("/trips/{id}", existingTripResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTripRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedTripResponse.getId()))
                .andExpect(jsonPath("$.name").value(updatedTripResponse.getName()))
                .andExpect(jsonPath("$.description").value(updatedTripResponse.getDescription()));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating a non-existent trip")
    void shouldReturnNotFoundWhenUpdatingNonExistentTrip() throws Exception {
        Long nonExistentId = 999L;
        doThrow(new ResourceNotFoundException("Trip not found with ID: " + nonExistentId))
                .when(tripService).updateTrip(eq(nonExistentId), any(TripRequest.class));

        mockMvc.perform(put("/trips/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTripRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid trip update")
    void shouldReturnBadRequestForInvalidTripUpdate() throws Exception {
        TripRequest invalidUpdateTripRequest = new TripRequest(
                "",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 10),
                "Invalid update"
        );

        mockMvc.perform(put("/trips/{id}", existingTripResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateTripRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test case for deleting an existing trip via DELETE request.
     * TDD Phase: RED
     * This test will FAIL initially because the DELETE /trips/{id} endpoint
     * is not yet implemented in TripController.
     */
    @Test
    @DisplayName("Should delete an existing trip and return 204 No Content")
    void shouldDeleteExistingTripAndReturnNoContent() throws Exception {
        Long tripIdToDelete = 300L;
        // GIVEN: The service successfully processes the delete request
        doNothing().when(tripService).deleteTrip(tripIdToDelete);

        // WHEN: Perform a DELETE request to "/trips/{id}"
        mockMvc.perform(delete("/trips/{id}", tripIdToDelete))
                // THEN: Expect HTTP 204 No Content status
                .andExpect(status().isNoContent());
    }

    /**
     * Test case for deleting a non-existent trip.
     * TDD Phase: RED
     * This test will FAIL initially because the controller might not handle
     * ResourceNotFoundException correctly during delete.
     */
    @Test
    @DisplayName("Should return 404 Not Found when deleting a non-existent trip")
    void shouldReturnNotFoundWhenDeletingNonExistentTrip() throws Exception {
        Long nonExistentId = 999L;
        // GIVEN: The service throws ResourceNotFoundException for a non-existent ID during delete
        doThrow(new ResourceNotFoundException("Trip not found with ID: " + nonExistentId))
                .when(tripService).deleteTrip(nonExistentId);

        // WHEN: Perform a DELETE request to "/trips/{id}"
        mockMvc.perform(delete("/trips/{id}", nonExistentId))
                // THEN: Expect HTTP 404 Not Found status
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Should export all trips to CSV and return 200 OK")
    void shouldExportAllTripsToCsv() throws Exception {
        // GIVEN: The trip service returns a list of trips, and CsvService converts them to bytes
        List<TripResponse> tripsToExport = Arrays.asList(existingTripResponse1, existingTripResponse2);
        when(tripService.getAllTrips()).thenReturn(tripsToExport);
        when(csvService.exportTripsToCsv(anyList())).thenReturn(sampleCsvBytesForExport);

        // WHEN: Perform a GET request to "/trips/export"
        mockMvc.perform(MockMvcRequestBuilders.get("/trips/export"))
                // THEN: Assert the response headers and content
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv")) // Expect CSV content type
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"trips.csv\"")) // Expect download header
                .andExpect(content().bytes(sampleCsvBytesForExport)); // Expect the correct CSV bytes
    }

    /**
     * Test case for importing trips from a CSV file.
     * TDD Phase: RED
     * This test will FAIL initially because the POST /trips/import endpoint
     * is not yet implemented in TripController.
     */
    @Test
    @DisplayName("Should import trips from CSV and return 200 OK with count")
    void shouldImportTripsFromCsv() throws Exception {
        // Create a MockMultipartFile to simulate a file upload
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", // parameter name in the request
                "trips_import.csv", // original filename
                "text/csv", // content type
                sampleCsvContentForImport.getBytes() // file content
        );

        // GIVEN: CsvService returns parsed TripRequests, and TripService saves them
        List<TripRequest> importedTripRequests = Arrays.asList(
                new TripRequest("Imported Trip 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5), "First imported trip"),
                new TripRequest("Imported Trip 2", LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 15), "Second imported trip")
        );
        when(csvService.importTripsFromCsv(any(InputStream.class))).thenReturn(importedTripRequests);
        // Simulate saving each imported trip (return a dummy response)
        when(tripService.createTrip(any(TripRequest.class)))
                .thenReturn(new TripResponse(99L, "Dummy", null, null, null)); // Dummy response for saving

        // WHEN: Perform a multipart POST request to "/trips/import"
        mockMvc.perform(multipart("/trips/import")
                        .file(csvFile))
                // THEN: Assert the response
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully imported 2 trips.")); // Expect success message with count
    }

    /**
     * Test case for importing trips from a malformed CSV file.
     * Ensures proper error handling for invalid file content.
     */
    @Test
    @DisplayName("Should return 400 Bad Request for malformed CSV import")
    void shouldReturnBadRequestForMalformedCsvImport() throws Exception {
        MockMultipartFile malformedCsvFile = new MockMultipartFile(
                "file",
                "malformed_trips.csv",
                "text/csv",
                "name,startDate\nBad Trip,2025-01-01\n".getBytes() // Missing endDate
        );

        // GIVEN: CsvService throws FileProcessingException for malformed input
        doThrow(new FileProcessingException("Failed to parse CSV file: Missing required CSV header: endDate"))
                .when(csvService).importTripsFromCsv(any(InputStream.class));

        // WHEN: Perform a multipart POST request
        mockMvc.perform(multipart("/trips/import")
                        .file(malformedCsvFile))
                .andExpect(status().isBadRequest()) // Expect 400 Bad Request
                .andExpect(content().string("Failed to parse CSV file: Missing required CSV header: endDate")); // Expect specific error message
    }

    /**
     * Test case for importing a non-CSV file type.
     * Ensures proper validation of content type.
     */
    @Test
    @DisplayName("Should return 400 Bad Request for non-CSV file type import")
    void shouldReturnBadRequestForNonCsvFileImport() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain", // Incorrect content type
                "This is not a CSV.".getBytes()
        );

        mockMvc.perform(multipart("/trips/import")
                        .file(textFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please upload a CSV file!"));
    }
}
