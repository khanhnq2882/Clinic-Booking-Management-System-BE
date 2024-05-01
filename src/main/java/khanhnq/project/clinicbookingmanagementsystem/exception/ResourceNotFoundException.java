package khanhnq.project.clinicbookingmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String fieldName, String fieldValue) {
        super(String.format("%s with value '%s' is not found. Please contact to admin.", fieldName, fieldValue));
    }
}
