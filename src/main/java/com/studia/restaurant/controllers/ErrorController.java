package com.studia.restaurant.controllers;

import com.studia.restaurant.domain.dtos.ErrorDto;
import com.studia.restaurant.exceptions.BaseException;
import com.studia.restaurant.exceptions.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * The ErrorController handles exceptions that occur in the application and provides appropriate responses.
 * It acts as a centralized error-handling mechanism using exception handlers for different exception types.
 *
 * Framework Annotations:
 * - RestController: Indicates that this class is a REST controller.
 * - ControllerAdvice: Allows centralized exception handling across the application.
 * - Slf4j: Provides logging capabilities.
 *
 * Exception Handlers:
 * 1. StorageException:
 *    Handles exceptions related to storage operations, such as saving or retrieving files.
 *    Responds with a 500 Internal Server Error.
 *
 * 2. BaseException:
 *    Handles general custom exceptions defined in the application.
 *    Responds with a 500 Internal Server Error.
 *
 * 3. Exception:
 *    Handles unhandled or unexpected exceptions in the application.
 *    Responds with a 500 Internal Server Error.
 *
 * Logging:
 * - Logs the details of the exceptions to the logger for debugging and tracking purposes.
 *
 * Response:
 * - Returns a structured error response using ErrorDto, containing:
 *   - HTTP status code of the error.
 *   - A user-friendly error message.
 */
@RestController
@ControllerAdvice
@Slf4j
public class ErrorController {

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorDto> handleStorageException(StorageException e) {
        log.error("Storage exception: {}", e);

    ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Unable to save or retrieve file")
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorDto> handleBaseException(BaseException e) {
        log.error("Base exception: {}", e);

        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error")
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(BaseException e) {
        log.error("Base exception: {}", e);

        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error")
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
