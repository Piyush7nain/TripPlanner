package com.example.tripplanner.model.factory;

import com.example.tripplanner.model.Destination;
import com.example.tripplanner.model.Trip;
import com.example.tripplanner.model.dto.DestinationRequest;
import com.example.tripplanner.model.dto.DestinationResponse;
import org.springframework.stereotype.Component;

@Component
public class DestinationFactory {

    public Destination createDestination(DestinationRequest destinationRequest, Trip trip) {
        Destination destination = new Destination();
        destination.setName(destinationRequest.getName());
        destination.setArrivalDate(destinationRequest.getArrivalDate());
        destination.setDepartureDate(destinationRequest.getDepartureDate());
        destination.setLocation(destinationRequest.getLocation());
        destination.setTrip(trip);
        return destination;
    }

    public DestinationResponse createDestinationResponse(Destination destination) {

        DestinationResponse destinationResponse = new DestinationResponse();
        destinationResponse.setId(destination.getId());
        destinationResponse.setName(destination.getName());
        destinationResponse.setArrivalDate(destination.getArrivalDate());
        destinationResponse.setDepartureDate(destination.getDepartureDate());
        destinationResponse.setTripId(destination.getTrip().getId());
        return destinationResponse;
    }

}
