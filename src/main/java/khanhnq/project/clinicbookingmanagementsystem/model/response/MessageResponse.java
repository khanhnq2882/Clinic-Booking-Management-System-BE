package khanhnq.project.clinicbookingmanagementsystem.model.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MessageResponse {
    public static ResponseEntity<String> getResponseMessage (String responseMessage, HttpStatus httpStatus) {
        return new ResponseEntity<>("{\"message\":\""+responseMessage+"\"}", httpStatus);
    }
}