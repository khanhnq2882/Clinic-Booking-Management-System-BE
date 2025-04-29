package khanhnq.project.clinicbookingmanagementsystem.service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.model.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.JwtResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.UserInfoResponse;

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