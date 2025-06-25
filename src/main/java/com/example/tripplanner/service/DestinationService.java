package com.example.tripplanner.service;

import com.example.tripplanner.model.dto.DestinationRequest;
import com.example.tripplanner.model.dto.DestinationResponse;

import java.util.List;

public interface DestinationService {

    /**
     * Adds a new destination to a specific trip.
     *
     * @param tripId The ID of the trip to add the destination to.
     * @param destinationRequest The DTO containing the details for the new destination.
     * @return The DTO representing the newly created destination.
     */
    DestinationResponse addDestinationToTrip(Long tripId, DestinationRequest destinationRequest);

    /**
     * Retrieves all destinations for a given trip.
     *
     * @param tripId The ID of the trip whose destinations are to be retrieved.
     * @return A list of DTOs representing the destinations for the specified trip.
     */
    List<DestinationResponse> getDestinationsForTrip(Long tripId);
}
