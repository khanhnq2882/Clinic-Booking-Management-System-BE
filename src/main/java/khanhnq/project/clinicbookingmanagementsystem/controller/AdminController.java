package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.entity.Skill;
import khanhnq.project.clinicbookingmanagementsystem.entity.Specialization;
import khanhnq.project.clinicbookingmanagementsystem.repository.SkillRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.SpecializationRepository;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    private final SkillRepository skillRepository;

    private final SpecializationRepository specializationRepository;

    public AdminController(AdminService adminService,
                           SkillRepository skillRepository,
                           SpecializationRepository specializationRepository) {
        this.adminService = adminService;
        this.skillRepository = skillRepository;
        this.specializationRepository = specializationRepository;
    }

    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(skillRepository.findAll());
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(specializationRepository.findAll());
    }

    @PostMapping("/update-user-roles/{userId}")
    public ResponseEntity<String> updateUserRoles(@PathVariable("userId") Long userId) {
        return adminService.updateUserRoles(userId);
    }
}
