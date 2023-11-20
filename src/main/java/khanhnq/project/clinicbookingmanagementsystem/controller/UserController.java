package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.entity.Skill;
import khanhnq.project.clinicbookingmanagementsystem.entity.Specialization;
import khanhnq.project.clinicbookingmanagementsystem.repository.SkillRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.SpecializationRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.UserRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final FileService fileService;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SpecializationRepository specializationRepository;


    public UserController(UserService userService,
                          FileService fileService,
                          UserRepository userRepository,
                          SkillRepository skillRepository,
                          SpecializationRepository specializationRepository) {
        this.userService = userService;
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.specializationRepository = specializationRepository;
    }

    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok().body(skillRepository.findAll());
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok().body(specializationRepository.findAll());
    }

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody UserProfileRequest userProfileRequest) {
        return userService.updateProfile(userProfileRequest);
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadFile(@RequestParam("avatar") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    @PostMapping(value = "/request-to-become-doctor")
    public ResponseEntity<String> requestBecomeDoctor(@RequestBody AddRoleDoctorRequest addRoleDoctorRequest) {
        return userService.requestBecomeDoctor(addRoleDoctorRequest);
    }

    @PostMapping("/upload-medical-license")
    public ResponseEntity<String> uploadMedicalLicense(@RequestParam("medicalLicense") MultipartFile file) {
        return userService.uploadMedicalLicense(file);
    }

    @PostMapping("/upload-medical-degree")
    public ResponseEntity<String> uploadMedicalDegree(@RequestParam("medicalDegree") MultipartFile file) {
        return userService.uploadMedicalDegree(file);
    }


}