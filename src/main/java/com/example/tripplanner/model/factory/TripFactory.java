package com.example.tripplanner.model.factory;

import com.example.tripplanner.model.Trip;
import com.example.tripplanner.model.dto.TripRequest;
import com.example.tripplanner.model.dto.TripResponse;
import org.springframework.stereotype.Component;

@Component()
public class TripFactory {

    public Trip createTripFromRequest(TripRequest tripRequest) {
        Trip trip = new Trip();
        trip.setName(tripRequest.getName());
        trip.setDescription(tripRequest.getDescription());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        return trip;
    }

    public TripResponse getTripResponse(Trip trip) {
        TripResponse tripResponse = new TripResponse();
        tripResponse.setId(trip.getId());
        tripResponse.setName(trip.getName());
        tripResponse.setDescription(trip.getDescription());
        tripResponse.setStartDate(trip.getStartDate());
        tripResponse.setEndDate(trip.getEndDate());
        return tripResponse;
    }

    public Trip updateTrip(Trip trip, TripRequest tripRequest) {
        trip.setName(tripRequest.getName());
        trip.setDescription(tripRequest.getDescription());
        trip.setStartDate(tripRequest.getStartDate());
        trip.setEndDate(tripRequest.getEndDate());
        return trip;
    }

}
