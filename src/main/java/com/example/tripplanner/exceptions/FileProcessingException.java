package com.example.tripplanner.exceptions;


import org.springframework.web.bind.annotation.ResponseStatus;

public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }
}
