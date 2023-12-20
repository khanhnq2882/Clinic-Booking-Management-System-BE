package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserInfoResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.common.MethodsCommon;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final MethodsCommon methodsCommon;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest, BindingResult bindingResult) {
        methodsCommon.handleErrors(bindingResult);
        return MessageResponse.getResponseMessage(authService.register(registerRequest), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        methodsCommon.handleErrors(bindingResult);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(authService.login(loginRequest));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, BindingResult bindingResult) {
        methodsCommon.handleErrors(bindingResult);
        return MessageResponse.getResponseMessage(authService.changePassword(changePasswordRequest), HttpStatus.OK);
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
        if (request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer ")) {
            SecurityContextHolder.clearContext();
        } else {
            return MessageResponse.getResponseMessage("Logout failed.", HttpStatus.OK);
        }
        return MessageResponse.getResponseMessage("Logout successfully.", HttpStatus.OK);
    }

}