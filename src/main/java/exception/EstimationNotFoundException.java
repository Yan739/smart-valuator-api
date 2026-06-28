package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an estimation cannot be found by the given ID or criteria.
 * Automatically mapped to HTTP 404 Not Found by GlobalHandlerException.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EstimationNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing estimation with the given numeric ID.
     *
     * @param id the ID that was not found
     */
    public EstimationNotFoundException(Long id) {
        super("Estimation with id " + id + " not found");
    }

    /**
     * Creates an exception with a custom error message.
     *
     * @param message descriptive error message
     */
    public EstimationNotFoundException(String message) {
        super(message);
    }
}
