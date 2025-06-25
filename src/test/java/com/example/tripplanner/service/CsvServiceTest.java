package com.example.tripplanner.service;

import com.example.tripplanner.exceptions.FileProcessingException;
import com.example.tripplanner.model.Trip;
import com.example.tripplanner.model.dto.TripRequest;
import com.example.tripplanner.model.dto.TripResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the CsvService.
 * These tests ensure that CSV data can be correctly converted to TripRequest DTOs
 * and TripResponse DTOs can be correctly converted to CSV bytes.
 */
@ExtendWith(MockitoExtension.class)
public class CsvServiceTest {

    @InjectMocks
    private CsvService csvService;

    private List<TripResponse> sampleTripResponses;
    private String sampleCsvContentForExport; // Renamed for clarity
    private String sampleCsvContentForImport; // Renamed for clarity
    private String malformedCsvContent;

    @BeforeEach
    void setUp() {
        // Sample TripResponse data for export testing
        sampleTripResponses = Arrays.asList(
                new TripResponse(1L, "Summer Trip", LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10), "Beach vacation"),
                new TripResponse(2L, "Winter Trip", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), "Skiing adventure")
        );

        // Expected CSV content for export testing (headers + data)
        // IMPORTANT: Using "\r\n" for CRLF (Windows-style) newlines, which is default for Apache Commons CSV
        sampleCsvContentForExport = "id,name,startDate,endDate,description\r\n" +
                "1,Summer Trip,2025-07-01,2025-07-10,Beach vacation\r\n" +
                "2,Winter Trip,2025-12-01,2025-12-05,Skiing adventure\r\n";

        // Sample CSV content for import testing
        // IMPORTANT: Using "\r\n" for CRLF (Windows-style) newlines
        String importCsvHeader = "name,startDate,endDate,description\r\n";
        String importCsvData1 = "Spring Break,2025-03-15,2025-03-20,Relaxing getaway\r\n";
        String importCsvData2 = "Autumn Colors,2025-10-01,2025-10-05,Leaf peeping tour\r\n";
        this.sampleCsvContentForImport = importCsvHeader + importCsvData1 + importCsvData2;

        // Malformed CSV content for import error testing (e.g., missing column)
        malformedCsvContent = "name,startDate,description\r\n" + // Missing endDate, using CRLF
                "Bad Trip,2025-01-01,Short trip\r\n";
    }

    /**
     * Test case for exporting trips to CSV.
     */
    @Test
    @DisplayName("Should export a list of TripResponse to CSV bytes")
    void shouldExportTripsToCsvBytes() throws IOException {
        byte[] csvBytes = csvService.exportTripsToCsv(sampleTripResponses);
        assertThat(new String(csvBytes)).isEqualTo(sampleCsvContentForExport); // Use new variable
    }

    /**
     * Test case for exporting an empty list of trips to CSV.
     * IMPORTANT: Updated expected string to use "\r\n"
     */
    @Test
    @DisplayName("Should export empty list to CSV with only headers")
    void shouldExportEmptyListToCsvWithHeaders() throws IOException {
        byte[] csvBytes = csvService.exportTripsToCsv(Collections.emptyList());
        // Expected: headers followed by a single CRLF newline
        assertThat(new String(csvBytes)).isEqualTo("id,name,startDate,endDate,description\r\n");
    }

    /**
     * Test case for importing trips from a valid CSV InputStream.
     */
    @Test
    @DisplayName("Should import trips from valid CSV InputStream")
    void shouldImportTripsFromValidCsvInputStream() throws IOException {
        InputStream is = new ByteArrayInputStream(sampleCsvContentForImport.getBytes()); // Use new variable
        List<TripRequest> importedTrips = csvService.importTripsFromCsv(is);

        assertThat(importedTrips).isNotNull();
        assertThat(importedTrips).hasSize(2);

        assertThat(importedTrips.get(0).getName()).isEqualTo("Spring Break");
        assertThat(importedTrips.get(0).getStartDate()).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(importedTrips.get(0).getEndDate()).isEqualTo(LocalDate.of(2025, 3, 20));
        assertThat(importedTrips.get(0).getDescription()).isEqualTo("Relaxing getaway");

        assertThat(importedTrips.get(1).getName()).isEqualTo("Autumn Colors");
        assertThat(importedTrips.get(1).getStartDate()).isEqualTo(LocalDate.of(2025, 10, 1));
        assertThat(importedTrips.get(1).getEndDate()).isEqualTo(LocalDate.of(2025, 10, 5));
        assertThat(importedTrips.get(1).getDescription()).isEqualTo("Leaf peeping tour");
    }

    /**
     * Test case for importing trips from a malformed CSV InputStream.
     */
    @Test
    @DisplayName("Should throw FileProcessingException for malformed CSV input")
    void shouldThrowFileProcessingExceptionForMalformedCsv() {
        InputStream is = new ByteArrayInputStream(malformedCsvContent.getBytes());
        assertThatThrownBy(() -> csvService.importTripsFromCsv(is))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("Failed to parse CSV file");
    }

    /**
     * Test case for importing trips from an empty CSV file.
     */
    @Test
    @DisplayName("Should return empty list for empty CSV input")
    void shouldReturnEmptyListForEmptyCsv() throws IOException {
        // Only header row with CRLF newline
        InputStream is = new ByteArrayInputStream("name,startDate,endDate,description\r\n".getBytes());
        List<TripRequest> importedTrips = csvService.importTripsFromCsv(is);
        assertThat(importedTrips).isNotNull();
        assertThat(importedTrips).isEmpty();
    }
}
