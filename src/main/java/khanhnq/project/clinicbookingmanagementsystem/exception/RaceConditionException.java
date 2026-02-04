package khanhnq.project.clinicbookingmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RaceConditionException extends RuntimeException {
    public RaceConditionException(String message) {
        super(message);
    }
}