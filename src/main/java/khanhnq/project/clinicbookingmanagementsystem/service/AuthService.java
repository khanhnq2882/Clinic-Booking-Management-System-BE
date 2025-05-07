package khanhnq.project.clinicbookingmanagementsystem.service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.model.request.AccountSystemRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ChangePasswordRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.LoginRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;

public interface AuthService {
    ResponseEntityBase register(RegisterRequest registerRequest);
    ResponseEntityBase newSystemAccount(AccountSystemRequest accountSystemRequest) throws MessagingException;
    ResponseEntityBase login(LoginRequest loginRequest);
    ResponseEntityBase logout(HttpServletRequest request);
    User getCurrentUser();
    ResponseEntityBase changePassword(ChangePasswordRequest changePasswordRequest);
    ResponseEntityBase forgotPassword(String email) throws MessagingException;
    ResponseEntityBase getUserByUsername (String username);
    ResponseEntityBase getUserInfo();
}