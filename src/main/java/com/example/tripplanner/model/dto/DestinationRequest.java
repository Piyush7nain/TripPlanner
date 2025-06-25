package com.example.tripplanner.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationRequest {

    @NotBlank(message = "Destination name is mandatory")
    private String name;

    @NotBlank(message = "Location is mandatory")
    private String location;

    @NotNull(message = "Arrival date is mandatory")
    @FutureOrPresent(message = "Arrival date cannot be in the past")
    private LocalDate arrivalDate;

    @NotNull(message = "Departure date is mandatory")
    @FutureOrPresent(message = "Departure date cannot be in the past")
    private LocalDate departureDate;
}