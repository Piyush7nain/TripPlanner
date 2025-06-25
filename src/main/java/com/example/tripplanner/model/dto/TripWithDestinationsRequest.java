package com.example.tripplanner.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripWithDestinationsRequest {
    @NotBlank(message = "Trip name is mandatory")
    @Size(max = 255, message = "Trip name cannot exceed 255 characters")
    private String name;

    @NotNull(message = "Start date is mandatory")
    private LocalDate startDate;

    @NotNull(message = "End date is mandatory")
    private LocalDate endDate;

    private String description;

    @Valid // Important: Ensures validation is applied to elements within the list as well
    private List<DestinationRequest> destinations; // List of destinations nested within the trip

    // Constructors
    public TripWithDestinationsRequest() {
    }

    public TripWithDestinationsRequest(String name, LocalDate startDate, LocalDate endDate, String description, List<DestinationRequest> destinations) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.destinations = destinations;
    }

}

