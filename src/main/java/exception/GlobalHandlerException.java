package exception;

import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for all REST controllers.
 * Converts exceptions into a consistent {@link ApiError} JSON response.
 */
@RestControllerAdvice
public class GlobalHandlerException {

    /** Returns 404 when an estimation is not found by ID. */
    @ExceptionHandler(EstimationNotFoundException.class)
    public ResponseEntity<ApiError> handleEstimationNotFoundException(@NonNull EstimationNotFoundException ex) {
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    /** Catch-all handler returning 500 for any unhandled exception. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception ex) {
        ApiError apiError = new ApiError(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /** Returns 404 when a JPA query returns no results. */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiError> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
        ApiError apiError = new ApiError(
                "No data found for the requested operation",
                HttpStatus.NOT_FOUND.value(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    /** Returns 409 Conflict when a database constraint is violated (e.g. duplicate key). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(@NonNull DataIntegrityViolationException ex) {
        ApiError apiError = new ApiError(
                "Data integrity violation: " + ex.getMostSpecificCause().getMessage(),
                HttpStatus.CONFLICT.value(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.CONFLICT);
    }


}
