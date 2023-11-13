package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/update-user-roles/{userId}")
    public ResponseEntity<String> updateUserRoles(@PathVariable("userId") Long userId) {
        return adminService.updateUserRoles(userId);
    }
}
