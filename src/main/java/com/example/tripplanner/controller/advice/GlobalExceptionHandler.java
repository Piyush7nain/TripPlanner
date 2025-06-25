package com.example.tripplanner.controller.advice;

import com.example.tripplanner.controller.TripController;
import com.example.tripplanner.exceptions.FileProcessingException;
import com.example.tripplanner.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ex.getMessage();
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>(); // Map to store field name and error message
        ex.getBindingResult().getFieldErrors().forEach(
                (error) -> {
                    // Cast to FieldError to get the specific field name
                    String fieldName = ((FieldError) error).getField();
                    // Get the default error message
                    String errorMessage = error.getDefaultMessage();
                    // Put the field name and error message into the map
                    errors.put(fieldName, errorMessage);
                }); // Populate map with field errors
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Return 400 Bad Request with error details
    }

    @ExceptionHandler(FileProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Assuming client-side file error
    public ResponseEntity<String> handleFileProcessingException(FileProcessingException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

