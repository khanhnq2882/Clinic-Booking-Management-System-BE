package khanhnq.project.clinicbookingmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceAlreadyExistException extends RuntimeException{
    public ResourceAlreadyExistException(String fieldName, String fieldValue) {
        super(String.format("%s '%s' is already exist.", fieldName, fieldValue));
    }
}
