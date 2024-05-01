package khanhnq.project.clinicbookingmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class FileUploadFailedException extends RuntimeException{
    public FileUploadFailedException(String message) {
        super(message);
    }
}
