package exception;

import tools.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standard error payload returned by the API for all handled exceptions.
 * Serialized as JSON with a human-readable timestamp.
 */
@Data
@AllArgsConstructor
public class ApiError {

    /** Human-readable description of the error. */
    private String message;

    /** HTTP status code associated with the error. */
    private int status;

    /**
     * Timestamp of when the error occurred.
     * Formatted as "dd-MM-yyyy HH:mm:ss" (24-hour clock).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp;
}
