package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.model.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.UserInfoResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/auth", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return MessageResponse.getResponseMessage(authService.register(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping("/new-system-account")
    public ResponseEntity<String> newSystemAccount(@Valid @RequestBody AccountSystemRequest accountSystemRequest) throws MessagingException {
        return MessageResponse.getResponseMessage(authService.newSystemAccount(accountSystemRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.login(loginRequest));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        return MessageResponse.getResponseMessage(authService.changePassword(changePasswordRequest), HttpStatus.OK);
    }

    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<String> forgotPassword(@PathVariable("email") String email) throws MessagingException {
        return MessageResponse.getResponseMessage(authService.forgotPassword(email), HttpStatus.OK);
    }

    @GetMapping("/get-user/{username}")
    public ResponseEntity<UserInfoResponse> getUserByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.getUserByUsername(username));
    }

    @GetMapping("/get-user-info")
    public ResponseEntity<UserInfoResponse> getUserInfo() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.getUserInfo());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return MessageResponse.getResponseMessage(authService.logout(request), HttpStatus.OK);
    }

}