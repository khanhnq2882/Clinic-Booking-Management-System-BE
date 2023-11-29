package khanhnq.project.clinicbookingmanagementsystem.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MessageResponse {

    private String responseMessage;

    public MessageResponse(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public static ResponseEntity<String> getResponseMessage (String responseMessage, HttpStatus httpStatus) {
        return new ResponseEntity<>(responseMessage, httpStatus);
    }

}