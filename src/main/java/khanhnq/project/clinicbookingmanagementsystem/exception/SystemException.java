package khanhnq.project.clinicbookingmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SystemException extends RuntimeException{
    public SystemException(String message) {
        super(message);
    }
}
