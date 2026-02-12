package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EstimationNotFoundException extends RuntimeException  {
    public EstimationNotFoundException(Long id) {
        super("Estimation with id " + id + " not found");
    }

    public EstimationNotFoundException(String message) {
        super(message);
    }
}
