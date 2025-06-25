package com.example.tripplanner.controller;

import com.example.tripplanner.exceptions.ResourceNotFoundException;
import com.example.tripplanner.model.dto.DestinationRequest;
import com.example.tripplanner.model.dto.DestinationResponse;
import com.example.tripplanner.service.DestinationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print; // For debugging
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the DestinationController, focusing on web layer interactions.
 * Uses @WebMvcTest to load only the web-related components, making tests fast and isolated.
 */
@WebMvcTest(DestinationController.class) // Specify DestinationController for testing
public class DestinationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Mock the DestinationService that the controller depends on
    private DestinationService destinationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingTripId;
    private DestinationRequest validDestinationRequest;
    private DestinationResponse createdDestinationResponse;
    private DestinationResponse existingDestinationResponse1;
    private DestinationResponse existingDestinationResponse2;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()); // Needed for LocalDate serialization

        existingTripId = 1L; // A dummy trip ID for testing

        validDestinationRequest = new DestinationRequest(
                "Tokyo Tower",
                "Tokyo, Japan",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 3)
        );

        createdDestinationResponse = new DestinationResponse(
                1L, // Simulated generated ID for destination
                "Tokyo Tower",
                "Tokyo, Japan",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 3),
                existingTripId
        );

        existingDestinationResponse1 = new DestinationResponse(
                101L,
                "Mount Fuji",
                "Japan",
                LocalDate.of(2025, 10, 5),
                LocalDate.of(2025, 10, 7),
                existingTripId
        );
        existingDestinationResponse2 = new DestinationResponse(
                102L,
                "Kyoto Temples",
                "Kyoto, Japan",
                LocalDate.of(2025, 10, 8),
                LocalDate.of(2025, 10, 10),
                existingTripId
        );
    }

    /**
     * Test case for adding a new destination to a trip.
     * TDD Phase: RED
     * This test will FAIL initially because the POST /trips/{tripId}/destinations endpoint
     * is not yet implemented in DestinationController.
     */
    @Test
    @DisplayName("Should add a destination to a trip and return 201 Created")
    void shouldAddDestinationToTripAndReturnCreated() throws Exception {
        // GIVEN: The service returns the created destination response
        when(destinationService.addDestinationToTrip(eq(existingTripId), any(DestinationRequest.class)))
                .thenReturn(createdDestinationResponse);
        String expectedLocationHeader = "http://localhost/trips/" + existingTripId + "/destinations/" + createdDestinationResponse.getId();

        // WHEN: Perform a POST request to the nested endpoint
        mockMvc.perform(post("/trips/{tripId}/destinations", existingTripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDestinationRequest)))
                // THEN: Assert the response
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(header().string("Location", expectedLocationHeader))
                .andExpect(jsonPath("$.id").value(createdDestinationResponse.getId()))
                .andExpect(jsonPath("$.name").value(createdDestinationResponse.getName()))
                .andExpect(jsonPath("$.location").value(createdDestinationResponse.getLocation()))
                .andExpect(jsonPath("$.tripId").value(createdDestinationResponse.getTripId()));
    }

    /**
     * Test case for adding a destination to a non-existent trip.
     * TDD Phase: RED
     * This test will FAIL initially because the controller might not handle
     * ResourceNotFoundException correctly when adding destination.
     */
    @Test
    @DisplayName("Should return 404 Not Found when adding destination to non-existent trip")
    void shouldReturnNotFoundWhenAddingDestinationToNonExistentTrip() throws Exception {
        Long nonExistentTripId = 999L;
        // GIVEN: The service throws ResourceNotFoundException
        doThrow(new ResourceNotFoundException("Trip not found with ID: " + nonExistentTripId))
                .when(destinationService).addDestinationToTrip(eq(nonExistentTripId), any(DestinationRequest.class));

        // WHEN: Perform a POST request
        mockMvc.perform(post("/trips/{tripId}/destinations", nonExistentTripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDestinationRequest)))
                .andExpect(status().isNotFound()); // Expect 404 Not Found
    }

    /**
     * Test case for adding a destination with invalid data.
     * Ensures validation works at the controller level.
     */
    @Test
    @DisplayName("Should return 400 Bad Request for invalid destination creation")
    void shouldReturnBadRequestForInvalidDestinationCreation() throws Exception {
        DestinationRequest invalidDestinationRequest = new DestinationRequest(
                "", // Blank name
                "Invalid Location",
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 11, 5)
        );

        mockMvc.perform(post("/trips/{tripId}/destinations", existingTripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDestinationRequest)))
                .andDo(print()) // Debugging: print response if test fails
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Destination name is mandatory"));
    }

    /**
     * Test case for retrieving all destinations for a specific trip.
     * TDD Phase: RED
     * This test will FAIL initially because the GET /trips/{tripId}/destinations endpoint
     * is not yet implemented in DestinationController.
     */
    @Test
    @DisplayName("Should retrieve all destinations for a trip and return 200 OK")
    void shouldRetrieveAllDestinationsForTripAndReturnOk() throws Exception {
        // GIVEN: The service returns a list of destinations for the trip
        List<DestinationResponse> allDestinations = Arrays.asList(existingDestinationResponse1, existingDestinationResponse2);
        when(destinationService.getDestinationsForTrip(existingTripId)).thenReturn(allDestinations);

        // WHEN: Perform a GET request to the nested endpoint
        mockMvc.perform(get("/trips/{tripId}/destinations", existingTripId))
                // THEN: Assert the response
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(existingDestinationResponse1.getId()))
                .andExpect(jsonPath("$[1].id").value(existingDestinationResponse2.getId()));
    }

    /**
     * Test case for retrieving destinations for a non-existent trip.
     * TDD Phase: RED
     * This test will FAIL initially because the controller might not handle
     * ResourceNotFoundException correctly when getting destinations.
     */
    @Test
    @DisplayName("Should return 404 Not Found when getting destinations for non-existent trip")
    void shouldReturnNotFoundWhenGettingDestinationsForNonExistentTrip() throws Exception {
        Long nonExistentTripId = 999L;
        // GIVEN: The service throws ResourceNotFoundException
        doThrow(new ResourceNotFoundException("Trip not found with ID: " + nonExistentTripId))
                .when(destinationService).getDestinationsForTrip(nonExistentTripId);

        // WHEN: Perform a GET request
        mockMvc.perform(get("/trips/{tripId}/destinations", nonExistentTripId))
                .andExpect(status().isNotFound()); // Expect 404 Not Found
    }

    /**
     * Test case for retrieving destinations for a trip that exists but has no destinations.
     */
    @Test
    @DisplayName("Should return empty array when trip exists but has no destinations")
    void shouldReturnEmptyArrayWhenTripHasNoDestinations() throws Exception {
        // GIVEN: The service returns an empty list
        when(destinationService.getDestinationsForTrip(existingTripId)).thenReturn(Collections.emptyList());

        // WHEN: Perform a GET request
        mockMvc.perform(get("/trips/{tripId}/destinations", existingTripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
