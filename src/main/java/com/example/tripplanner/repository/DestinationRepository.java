package com.example.tripplanner.repository;

import com.example.tripplanner.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByTripId(Long tripId);
}
