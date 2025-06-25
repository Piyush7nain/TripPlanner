package com.example.tripplanner.service.impl;

import com.example.tripplanner.exceptions.ResourceNotFoundException;
import com.example.tripplanner.model.Trip;
import com.example.tripplanner.model.dto.TripRequest;
import com.example.tripplanner.model.dto.TripResponse;
import com.example.tripplanner.model.factory.TripFactory;
import com.example.tripplanner.repository.TripRepository;
import com.example.tripplanner.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripFactory tripFactory;

    @Autowired
    public TripServiceImpl(TripRepository tripRepository, TripFactory tripFactory) {
        this.tripRepository = tripRepository;
        this.tripFactory = tripFactory;
    }

    @Override
    public TripResponse createTrip(TripRequest tripRequest) {
        Trip trip = tripFactory.createTripFromRequest(tripRequest);
        Trip savedTrip = tripRepository.save(trip);
        return tripFactory.getTripResponse(savedTrip);
    }


    @Override
    public List<TripResponse> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();
        return trips.stream().map(tripFactory::getTripResponse).collect(Collectors.toList());
    }

    @Override
    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id).orElseThrow(() ->  new ResourceNotFoundException("Trip not found with ID: " + id));
        return tripFactory.getTripResponse(trip);
    }

    @Override
    public TripResponse updateTrip(Long id, TripRequest tripRequest) {
        Trip existingTrip = tripRepository.findById(id).orElseThrow(() ->  new ResourceNotFoundException("Trip not found with ID: " + id));
        Trip updatedTrip = tripFactory.updateTrip(existingTrip, tripRequest);

        Trip savedTrip = tripRepository.save(updatedTrip);
        return tripFactory.getTripResponse(savedTrip);
    }

    @Override
    public void deleteTrip(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found with ID: " + id);
        }
        tripRepository.deleteById(id);
    }
}
