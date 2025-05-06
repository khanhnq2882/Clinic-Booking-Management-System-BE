package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.model.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
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
    public ResponseEntity<ResponseEntityBase> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.register(registerRequest));
    }

    @PostMapping("/new-system-account")
    public ResponseEntity<ResponseEntityBase> newSystemAccount(@Valid @RequestBody AccountSystemRequest accountSystemRequest) throws MessagingException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.newSystemAccount(accountSystemRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseEntityBase> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.login(loginRequest));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseEntityBase> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.changePassword(changePasswordRequest));
    }

    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<ResponseEntityBase> forgotPassword(@PathVariable("email") String email) throws MessagingException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.forgotPassword(email));
    }

    @GetMapping("/get-user/{username}")
    public ResponseEntity<ResponseEntityBase> getUserByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.getUserByUsername(username));
    }

    @GetMapping("/get-user-info")
    public ResponseEntity<ResponseEntityBase> getUserInfo() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.getUserInfo());
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseEntityBase> logout(HttpServletRequest request) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.logout(request));
    }

}