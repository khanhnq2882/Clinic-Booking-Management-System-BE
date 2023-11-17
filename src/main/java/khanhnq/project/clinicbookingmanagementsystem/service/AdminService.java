package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.entity.Skill;
import khanhnq.project.clinicbookingmanagementsystem.response.RequestDoctorResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdminService {
    ResponseEntity<String> updateUserRoles(Long userId);
    ResponseEntity<List<UserResponse>> getAllUsers();
    ResponseEntity<List<RequestDoctorResponse>> getAllRequestDoctors();

}
