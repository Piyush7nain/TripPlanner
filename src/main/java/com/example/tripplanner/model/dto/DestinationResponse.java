package com.example.tripplanner.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for sending Destination data to the client.
 * Used to represent destination details in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationResponse {
    private Long id;
    private String name;
    private String location;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private Long tripId; // Include trip ID for context
}
