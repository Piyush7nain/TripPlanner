package com.example.tripplanner.controller;

import com.example.tripplanner.exceptions.FileProcessingException;
import com.example.tripplanner.model.dto.DestinationRequest;
import com.example.tripplanner.model.dto.TripRequest;
import com.example.tripplanner.model.dto.TripResponse;
import com.example.tripplanner.model.dto.TripWithDestinationsRequest;
import com.example.tripplanner.service.CsvService;
import com.example.tripplanner.service.DestinationService;
import com.example.tripplanner.service.JsonService;
import com.example.tripplanner.service.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {
    private final TripService tripService;
    private final CsvService csvService;
    private final JsonService jsonService; // Inject JsonService
    private final DestinationService destinationService; // Inject DestinationService to add nested destinations

    @Autowired
    public TripController(TripService tripService, CsvService csvService, JsonService jsonService, DestinationService destinationService) {
        this.tripService = tripService;
        this.csvService = csvService;
        this.jsonService = jsonService;
        this.destinationService = destinationService;
    }

    @PostMapping("/createTrip")
    public ResponseEntity<TripResponse> createTrip(@RequestBody @Valid TripRequest tripRequest) {
        TripResponse tripResponse = tripService.createTrip(tripRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tripResponse.getId())
                .toUri();
        return ResponseEntity.created(location).body(tripResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        TripResponse tripResponse = tripService.getTripById(id);
        return ResponseEntity.ok(tripResponse);
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getAllTrips() {
        List<TripResponse> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable Long id, @RequestBody @Valid TripRequest tripRequest) {
        TripResponse updatedTrip = tripService.updateTrip(id, tripRequest);
        return ResponseEntity.ok(updatedTrip);
    }

    @DeleteMapping("/{id}") // Maps DELETE requests to /trips/{id}
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id); // Service layer handles not found
        return ResponseEntity.noContent().build(); // Return 204 No Content
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTripsToCsv() throws IOException {
        // 1. Get all trips from the service layer
        List<TripResponse> trips = tripService.getAllTrips();
        // 2. Convert trips to CSV bytes using CsvService
        byte[] csvBytes = csvService.exportTripsToCsv(trips);

        // 3. Set HTTP headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv")); // Set content type to CSV
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("trips.csv").build()); // Suggest filename for download
        headers.setContentLength(csvBytes.length); // Set content length

        // Return the CSV bytes with appropriate headers and 200 OK status
        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    /**
     * Imports trips from an uploaded CSV file.
     * Handles HTTP POST requests to /trips/import.
     *
     * @param file The uploaded CSV file as a MultipartFile.
     * @return A ResponseEntity with a success message and count of imported trips.
     * @throws IOException If an I/O error occurs reading the file.
     * @throws FileProcessingException If the CSV content is malformed.
     */
    @PostMapping("/import")
    public ResponseEntity<String> importTripsFromCsv(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. Validate file type
        if (!"text/csv".equals(file.getContentType())) {
            return new ResponseEntity<>("Please upload a CSV file!", HttpStatus.BAD_REQUEST);
        }

        // 2. Read and parse CSV using CsvService
        List<TripRequest> importedTripRequests;
        try {
            importedTripRequests = csvService.importTripsFromCsv(file.getInputStream());
        } catch (FileProcessingException e) {
            // Re-throw custom exception to be caught by GlobalExceptionHandler
            throw e;
        } catch (IOException e) {
            // Wrap general IO exceptions in our custom exception
            throw new FileProcessingException("Failed to read uploaded file: " + e.getMessage());
        }

        // 3. Create each imported trip using the TripService
        for (TripRequest tripRequest : importedTripRequests) {
            tripService.createTrip(tripRequest); // Assuming createTrip handles validation and saving
        }

        // Return success message with count
        return new ResponseEntity<>("Successfully imported " + importedTripRequests.size() + " trips.", HttpStatus.OK);
    }

    @PostMapping("/import-json")
    public ResponseEntity<String> importTripsWithDestinationsFromJson(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. Validate file type
        if (!"application/json".equals(file.getContentType())) {
            return new ResponseEntity<>("Please upload a JSON file!", HttpStatus.BAD_REQUEST);
        }

        // 2. Read and parse JSON using JsonService
        List<TripWithDestinationsRequest> importedTripWithDestinationsRequests;
        try {
            importedTripWithDestinationsRequests = jsonService.importTripsWithDestinationsFromJson(file.getInputStream());
        } catch (FileProcessingException e) {
            // Re-throw custom exception to be caught by GlobalExceptionHandler
            throw e;
        } catch (IOException e) {
            // Wrap general IO exceptions in our custom exception
            throw new FileProcessingException("Failed to read uploaded file: " + e.getMessage());
        }

        // 3. Process each trip and its destinations
        int importedTripCount = 0;
        for (TripWithDestinationsRequest tripWithDestinationsRequest : importedTripWithDestinationsRequests) {
            // Create the main Trip
            TripRequest tripRequest = new TripRequest(
                    tripWithDestinationsRequest.getName(),
                    tripWithDestinationsRequest.getStartDate(),
                    tripWithDestinationsRequest.getEndDate(),
                    tripWithDestinationsRequest.getDescription()
            );
            TripResponse createdTrip = tripService.createTrip(tripRequest); // This will save the trip to DB and return its ID

            // Add destinations to the newly created trip
            if (tripWithDestinationsRequest.getDestinations() != null && !tripWithDestinationsRequest.getDestinations().isEmpty()) {
                for (DestinationRequest destinationRequest : tripWithDestinationsRequest.getDestinations()) {
                    // Use the ID of the just-created trip
                    destinationService.addDestinationToTrip(createdTrip.getId(), destinationRequest);
                }
            }
            importedTripCount++;
        }

        // Return success message with count
        return new ResponseEntity<>("Successfully imported " + importedTripCount + " trips with their destinations.", HttpStatus.OK);
    }


    @PostMapping("/batch") // Maps POST requests to /trips/batch
    public ResponseEntity<String> importBatchTrips(
            @Valid @RequestBody List<TripWithDestinationsRequest> batchTrips) { // @Valid on the list for nested validation

        int importedTripCount = 0;
        for (TripWithDestinationsRequest tripWithDestinationsRequest : batchTrips) {
            // Create the main Trip
            TripRequest tripRequest = new TripRequest(
                    tripWithDestinationsRequest.getName(),
                    tripWithDestinationsRequest.getStartDate(),
                    tripWithDestinationsRequest.getEndDate(),
                    tripWithDestinationsRequest.getDescription()
            );
            TripResponse createdTrip = tripService.createTrip(tripRequest); // This will save the trip to DB and return its ID

            // Add destinations to the newly created trip
            if (tripWithDestinationsRequest.getDestinations() != null && !tripWithDestinationsRequest.getDestinations().isEmpty()) {
                for (DestinationRequest destinationRequest : tripWithDestinationsRequest.getDestinations()) {
                    // Use the ID of the just-created trip
                    destinationService.addDestinationToTrip(createdTrip.getId(), destinationRequest);
                }
            }
            importedTripCount++;
        }

        return new ResponseEntity<>("Successfully imported " + importedTripCount + " trips with their destinations.", HttpStatus.OK);
    }
}
