package com.studia.restaurant.controllers;
import com.studia.restaurant.domain.dtos.ErrorDto;
import com.studia.restaurant.exceptions.BaseException;
import com.studia.restaurant.exceptions.RestaurantNotFoundException;
import com.studia.restaurant.exceptions.ReviewNotAllowedException;
import com.studia.restaurant.exceptions.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@ControllerAdvice
@Slf4j
public class ErrorController {

    /**
     * Handles situations where a user attempts to review a restaurant but is not allowed to do so.
     * Logs the exception and returns a forbidden HTTP status with an error message.
     *
     * @param e the exception indicating the user is not allowed to review the restaurant
     * @return a ResponseEntity containing an ErrorDto with details about the error and a 403 Forbidden status
     */
    @ExceptionHandler(ReviewNotAllowedException.class)
    public ResponseEntity<ErrorDto> handleReviewNotAllowedException(ReviewNotAllowedException e) {
        log.error("Review not allowed", e);

        ErrorDto errorDto = new ErrorDto().builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("You are not allowed to review this restaurant.")
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles the RestaurantNotFoundException, which is thrown when a requested restaurant
     * cannot be found in the system. Logs the error and returns a standardized error response
     * containing HTTP status code 404 along with a descriptive message.
     *
     * @param e the exception object containing details about the RestaurantNotFoundException
     * @return a ResponseEntity containing the ErrorDto with the HTTP status code 404 and an error message
     */
    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorDto> handleRestaurantNotFoundException(RestaurantNotFoundException e) {
        log.error("Restaurant not found", e);

        ErrorDto errorDto = new ErrorDto().builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message("The specified restaurant does not exist.")
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }


    /**
     * Handles exceptions of type {@link MethodArgumentNotValidException}, which are thrown when validation
     * on an argument annotated with {@code @Valid} fails.
     *
     * @param ex the exception containing details about the validation errors
     * @return a {@link ResponseEntity} containing an {@link ErrorDto} object with the status code and
     *         a detailed error message summarizing all validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Caught MethodArgumentNotValidException", ex);

       String errorMessage = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(",  "));

        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .build();


        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exceptions of type StorageException that occur within the application.
     * Logs the exception and returns a standardized error response containing an error message
     * and an HTTP status of INTERNAL_SERVER_ERROR (500).
     *
     * @param ex the StorageException that was caught
     * @return a ResponseEntity containing an ErrorDto with the error details and an HTTP status of 500
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorDto> handleStorageException(StorageException ex) {
        log.error("Caught StorageException", ex);

        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Unable to save or retrieve resources at this time")
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles exceptions of type BaseException within the application.
     * This method captures the exception, logs the details, and constructs a response entity
     * with an appropriate error message and HTTP status code.
     *
     * @param ex the instance of BaseException that was thrown
     * @return a ResponseEntity containing an ErrorDto object with the error details,
     *         including a 500 Internal Server Error status code and a generic error message
     */
    // Handle our base application exception
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorDto> handleBaseException(BaseException ex) {
        log.error("Caught BaseException", ex);

        ErrorDto error = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles any unexpected exceptions that occur in the application.
     * This is a catch-all exception handler that logs the exception and returns
     * a generic error response.
     *
     * @param ex the exception that was caught
     * @return a ResponseEntity containing an ErrorDto with a status code of 500 (Internal Server Error)
     *         and a message indicating that an unexpected error occurred
     */
    // Catch-all for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Caught unexpected exception", ex);

        ErrorDto error = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
