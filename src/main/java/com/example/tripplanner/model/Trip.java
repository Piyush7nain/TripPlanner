package com.example.tripplanner.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    // 'mappedBy' indicates the field in the 'Destination' entity that owns the relationship.
    // 'cascade = CascadeType.ALL' means operations (persist, merge, remove) on Trip will cascade to Destinations.
    // 'orphanRemoval = true' means if a Destination is removed from the 'destinations' list, it will be deleted from the database.
    @JsonManagedReference // Prevents infinite recursion when serializing Trip -> Destinations
    private List<Destination> destinations = new ArrayList<>();

    public Trip(Long id, String name, LocalDate startDate, LocalDate endDate, String description) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }
}
