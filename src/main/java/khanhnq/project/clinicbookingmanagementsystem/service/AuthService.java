package khanhnq.project.clinicbookingmanagementsystem.service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserInfoResponse;

public interface AuthService {
    String register(RegisterRequest registerRequest);
    String newSystemAccount(AccountSystemRequest accountSystemRequest) throws MessagingException;
    JwtResponse login(LoginRequest loginRequest);
    String logout(HttpServletRequest request);
    User getCurrentUser();
    String changePassword(ChangePasswordRequest changePasswordRequest);
    String forgotPassword(String email) throws MessagingException;
    UserInfoResponse getUserByUsername (String username);
    UserInfoResponse getUserInfo();
}