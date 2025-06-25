package com.example.tripplanner.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.tripplanner.model.dto.DestinationRequest;
import com.example.tripplanner.model.dto.DestinationResponse;
import com.example.tripplanner.service.DestinationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/trips/{tripId}/destinations")
public class DestinationController {

    private final DestinationService destinationService;


    @Autowired
    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @PostMapping // Maps POST requests to /trips/{tripId}/destinations
    public ResponseEntity<DestinationResponse> addDestinationToTrip(
            @PathVariable Long tripId, // Extract tripId from the path
            @Valid @RequestBody DestinationRequest destinationRequest) {

        DestinationResponse createdDestination = destinationService.addDestinationToTrip(tripId, destinationRequest);

        // Build the URI for the newly created resource (e.g., /trips/1/destinations/10)
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri() // Starts with the current request URI (/trips/{tripId}/destinations)
                .path("/{id}") // Appends the ID path variable for the new destination
                .buildAndExpand(createdDestination.getId()) // Replaces {id} with the actual destination ID
                .toUri(); // Builds the final URI

        return ResponseEntity.created(location).body(createdDestination);
    }

    @GetMapping // Maps GET requests to /trips/{tripId}/destinations
    public ResponseEntity<List<DestinationResponse>> getDestinationsForTrip(@PathVariable Long tripId) {
        List<DestinationResponse> destinations = destinationService.getDestinationsForTrip(tripId);
        return ResponseEntity.ok(destinations);
    }
}
