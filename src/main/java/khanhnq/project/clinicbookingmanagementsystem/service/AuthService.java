package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserInfoResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    String register(RegisterRequest registerRequest);
    JwtResponse login(LoginRequest loginRequest);
    User getCurrentUser();
    ResponseEntity<String> changePassword(ChangePasswordRequest changePasswordRequest);
    UserInfoResponse getUserInfo();
}