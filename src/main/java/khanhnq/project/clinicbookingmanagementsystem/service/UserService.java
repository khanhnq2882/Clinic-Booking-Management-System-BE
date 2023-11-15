package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.FileRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    ResponseEntity<String> updateProfile(UserProfileRequest userProfileRequest);
    ResponseEntity<String> uploadAvatar(MultipartFile file);
    ResponseEntity<String> requestBecomeDoctor(AddRoleDoctorRequest addRoleDoctorRequest);
    ResponseEntity<String> uploadLicenseDegree(FileRequest fileRequest);


}