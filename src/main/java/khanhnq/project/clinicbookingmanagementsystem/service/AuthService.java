package khanhnq.project.clinicbookingmanagementsystem.service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.model.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import java.net.UnknownHostException;

public interface AuthService {
    ResponseEntityBase register(RegisterRequest registerRequest);
    ResponseEntityBase newSystemAccount(AccountSystemRequest accountSystemRequest) throws MessagingException;
    ResponseEntityBase login(LoginRequest loginRequest) throws UnknownHostException;
    ResponseEntityBase refreshToken(String token);
    ResponseEntityBase logout(HttpServletRequest request);
    ResponseEntityBase changePassword(ChangePasswordRequest changePasswordRequest);
    ResponseEntityBase forgotPassword(String email) throws MessagingException;
    ResponseEntityBase getUserByUsername (String username);
    ResponseEntityBase getUserInfo();
    User getCurrentUser();
}