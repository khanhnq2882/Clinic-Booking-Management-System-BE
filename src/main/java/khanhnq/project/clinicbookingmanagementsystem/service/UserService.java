package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<String> updateProfile(UserProfileRequest userProfileRequest);
}