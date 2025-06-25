package com.example.tripplanner.service;
import com.example.tripplanner.exceptions.FileProcessingException;
import com.example.tripplanner.model.dto.TripWithDestinationsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // For pretty printing (optional)
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // For LocalDate support
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service for handling JSON file operations (import) for Trip and Destination data.
 * Uses Jackson (ObjectMapper) for JSON parsing.
 */
@Service
public class JsonService {

    private final ObjectMapper objectMapper;

    /**
     * Constructor. Configures ObjectMapper for date handling.
     */
    public JsonService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Register module for LocalDate
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Ensure dates are in "yyyy-MM-dd" format
    }

    /**
     * Imports a list of TripWithDestinationsRequest DTOs from a JSON InputStream.
     * The JSON is expected to be an array of trip objects, each potentially containing a list of destinations.
     *
     * @param is The InputStream containing the JSON data.
     * @return A list of TripWithDestinationsRequest DTOs parsed from the JSON.
     * @throws com.example.tripplanner.exceptions.FileProcessingException If the JSON file is malformed or an I/O error occurs.
     */
    public List<TripWithDestinationsRequest> importTripsWithDestinationsFromJson(InputStream is) {
        try {
            // Read the InputStream and map it to a List of TripWithDestinationsRequest
            // We use readValue(InputStream, TypeReference) for deserializing generic types like List<T>
            return objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, TripWithDestinationsRequest.class));
        } catch (JsonProcessingException e) {
            // Catch errors specific to JSON parsing (e.g., malformed JSON, type mismatches)
            throw new FileProcessingException("Failed to parse JSON file: " + e.getMessage());
        } catch (IOException e) {
            // Catch general I/O errors
            throw new FileProcessingException("Failed to read JSON file: " + e.getMessage());
        }
    }
}