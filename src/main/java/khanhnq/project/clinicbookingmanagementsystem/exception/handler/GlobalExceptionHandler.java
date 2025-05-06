package khanhnq.project.clinicbookingmanagementsystem.exception.handler;

import khanhnq.project.clinicbookingmanagementsystem.exception.*;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ResponseEntityBase> handlerResourceAlreadyExistException(ResourceAlreadyExistException exception) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseEntityBase> handlerResourceAlreadyExistException(ResourceNotFoundException exception) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.NOT_FOUND.value(), exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileUploadFailedException.class)
    public ResponseEntity<ResponseEntityBase> handlerFileUploadFailedException(FileUploadFailedException exception) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.EXPECTATION_FAILED.value(), exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ResponseEntityBase> handlerUnauthorizedException(UnauthorizedException exception) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.UNAUTHORIZED.value(), exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ResponseEntityBase> handlerForbiddenException(ForbiddenException exception) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.FORBIDDEN.value(), exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ResponseEntityBase> handlerSystemException(SystemException exception) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            validationErrors.put(((FieldError) error).getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

}
