package com.example.tripplanner.service.impl;

import com.example.tripplanner.exceptions.ResourceNotFoundException;
import com.example.tripplanner.model.Destination;
import com.example.tripplanner.model.Trip;
import com.example.tripplanner.model.dto.DestinationRequest;
import com.example.tripplanner.model.dto.DestinationResponse;
import com.example.tripplanner.model.factory.DestinationFactory;
import com.example.tripplanner.repository.DestinationRepository;
import com.example.tripplanner.repository.TripRepository;
import com.example.tripplanner.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DestinationServiceImpl implements DestinationService {
    private final DestinationRepository destinationRepository;
    private final TripRepository tripRepository;
    private final DestinationFactory destinationFactory;

    @Autowired
    public DestinationServiceImpl(DestinationRepository destinationRepository, TripRepository tripRepository, DestinationFactory destinationFactory) {
        this.destinationRepository = destinationRepository;
        this.tripRepository = tripRepository;
        this.destinationFactory = destinationFactory;
    }

    @Override
    public DestinationResponse addDestinationToTrip(Long tripId, DestinationRequest destinationRequest) {
        // 1. Find the parent Trip
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        // 2. Convert DestinationRequest DTO to Destination Entity
        Destination destination = destinationFactory.createDestination(destinationRequest, trip);

        // 3. Save the Destination entity
        Destination savedDestination = destinationRepository.save(destination);

        // 4. Convert the saved Destination entity back to DestinationResponse DTO
        return destinationFactory.createDestinationResponse(savedDestination); // Get trip ID from the associated trip
    }

    @Override
    public List<DestinationResponse> getDestinationsForTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
        }

        // 2. Find all destinations associated with the tripId
        List<Destination> destinations = destinationRepository.findByTripId(tripId);

        // 3. Convert Destination entities to DestinationResponse DTOs
        return destinations.stream()
                .map(destinationFactory::createDestinationResponse)
                .collect(Collectors.toList());

    }
}
