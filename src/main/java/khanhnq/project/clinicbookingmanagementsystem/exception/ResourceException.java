package khanhnq.project.clinicbookingmanagementsystem.exception;

import org.springframework.http.HttpStatus;

public class ResourceException extends RuntimeException {
    private HttpStatus httpStatus;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ResourceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}