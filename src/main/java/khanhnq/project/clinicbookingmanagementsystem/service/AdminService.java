package khanhnq.project.clinicbookingmanagementsystem.service;

import org.springframework.http.ResponseEntity;

public interface AdminService {
    ResponseEntity<String> updateUserRoles(Long userId);

}
