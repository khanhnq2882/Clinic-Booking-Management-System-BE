package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.entity.Skill;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    ResponseEntity<String> updateUserRoles(Long userId);

}
