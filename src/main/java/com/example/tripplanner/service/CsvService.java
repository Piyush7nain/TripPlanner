package com.example.tripplanner.service;

import org.springframework.stereotype.Service;
import com.example.tripplanner.exceptions.FileProcessingException;
import com.example.tripplanner.model.dto.TripRequest;
import com.example.tripplanner.model.dto.TripResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
@Service
public class CsvService {

    // CSV Headers for export
    private static final String[] EXPORT_HEADERS = {"id", "name", "startDate", "endDate", "description"};
    // CSV Headers for import (ID is not expected in import file)
    private static final String[] IMPORT_HEADERS = {"name", "startDate", "endDate", "description"};

    public byte[] exportTripsToCsv(List<TripResponse> trips) throws IOException {
        // Use ByteArrayOutputStream to write CSV data to memory
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter pw = new PrintWriter(baos);
             // Configure CSVPrinter with the specified format and headers
             CSVPrinter csvPrinter = new CSVPrinter(pw, CSVFormat.DEFAULT.withHeader(EXPORT_HEADERS))) {

            // Iterate through each trip and print its fields to the CSV
            for (TripResponse trip : trips) {
                csvPrinter.printRecord(
                        trip.getId(),
                        trip.getName(),
                        trip.getStartDate(),
                        trip.getEndDate(),
                        trip.getDescription()
                );
            }
            csvPrinter.flush(); // Ensure all data is written to the PrintWriter
            return baos.toByteArray(); // Return the CSV data as a byte array
        }
    }

    public List<TripRequest> importTripsFromCsv(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             // Configure CSVParser to parse with headers, ignoring missing headers (flexible for import)
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<TripRequest> tripRequests = new ArrayList<>();
            List<CSVRecord> csvRecords = csvParser.getRecords();

            // Check if all required headers are present (optional but good for robustness)
            for (String header : IMPORT_HEADERS) {
                if (!csvParser.getHeaderMap().containsKey(header)) {
                    throw new FileProcessingException("Missing required CSV header: " + header);
                }
            }

            // Iterate through each record in the CSV
            for (CSVRecord csvRecord : csvRecords) {
                try {
                    // Parse fields from the CSV record by header name
                    String name = csvRecord.get("name");
                    LocalDate startDate = LocalDate.parse(csvRecord.get("startDate"));
                    LocalDate endDate = LocalDate.parse(csvRecord.get("endDate"));
                    String description = csvRecord.get("description");

                    // Create a new TripRequest DTO
                    TripRequest tripRequest = new TripRequest(name, startDate, endDate, description);
                    tripRequests.add(tripRequest);
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    // Catch errors during parsing of individual fields (e.g., missing column, bad date format)
                    throw new FileProcessingException("Error parsing CSV record: " + csvRecord.getRecordNumber() + ". " + e.getMessage());
                }
            }
            return tripRequests;
        } catch (IOException e) {
            // Catch general I/O errors
            throw new FileProcessingException("Failed to read CSV file: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected parsing errors from Commons CSV
            throw new FileProcessingException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}
