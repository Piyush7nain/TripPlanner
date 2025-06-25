package com.example.tripplanner.service;

import com.example.tripplanner.model.dto.TripRequest;
import com.example.tripplanner.model.dto.TripResponse;

import java.util.List;

public interface TripService {
    TripResponse createTrip(TripRequest tripRequest);
    List<TripResponse> getAllTrips();
    TripResponse getTripById(Long id);
    TripResponse updateTrip(Long id, TripRequest tripRequest);
    void deleteTrip(Long id);
}
