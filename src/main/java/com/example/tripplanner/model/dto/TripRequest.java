package com.example.tripplanner.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripRequest {

    @NotBlank(message = "Trip Name must not be blank")
    private String name;

    @NotNull(message = "Start Date is mandatory")
    @FutureOrPresent(message = "Start date cannot be in past")
    private LocalDate startDate;

    @NotNull(message = "End Date is mandatory")
    @FutureOrPresent(message = "End date cannot be in past")
    private LocalDate endDate;

    private String description;

}
