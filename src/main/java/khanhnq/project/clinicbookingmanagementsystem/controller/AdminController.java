package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import khanhnq.project.clinicbookingmanagementsystem.entity.Skill;
import khanhnq.project.clinicbookingmanagementsystem.repository.SkillRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.SpecializationRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final AuthService authService;
    private final FileService fileService;
    private final SkillRepository skillRepository;
    private final SpecializationRepository specializationRepository;

    public AdminController(AdminService adminService,
                           SkillRepository skillRepository,
                           SpecializationRepository specializationRepository,
                           AuthService authService,
                           FileService fileService) {
        this.adminService = adminService;
        this.skillRepository = skillRepository;
        this.specializationRepository = specializationRepository;
        this.authService = authService;
        this.fileService = fileService;
    }

    @PostMapping("/approve-request-doctor/{userId}")
    public ResponseEntity<String> approveRequestDoctor(@PathVariable("userId") Long userId) {
        return adminService.approveRequestDoctor(userId);
    }

    @PostMapping("/reject-request-doctor/{userId}")
    public ResponseEntity<String> rejectRequestDoctor(@PathVariable("userId") Long userId) {
        return adminService.rejectRequestDoctor(userId);
    }

    @GetMapping("/get-all-users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/get-all-request-doctors")
    public ResponseEntity<List<RequestDoctorResponse>> getAllRequestDoctors() {
        return adminService.getAllRequestDoctors();
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long fileId) {
        File file = fileService.getFileById(fileId);
        String fileName = file.getFilePath().split("/")[2];
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(file.getData());
    }

    @GetMapping("/get-all-doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return adminService.getAllDoctors();
    }

    @GetMapping("/get-all-specializations")
    public ResponseEntity<List<SpecializationResponse>> getAllSpecializations() {
        return adminService.getAllSpecializations();
    }

    @GetMapping("/get-all-service-categories/{specializationId}")
    public ResponseEntity<List<ServiceCategoryResponse>> getAllServiceCategories(@PathVariable("specializationId") Long specializationId) {
        return adminService.getAllServiceCategories(specializationId);
    }

    @PostMapping("/add-service-category")
    public ResponseEntity<String> addServiceCategory(@RequestBody ServiceCategoryRequest serviceCategoryRequest) {
        return adminService.addServiceCategory(serviceCategoryRequest);
    }

    @PostMapping("/add-service")
    public ResponseEntity<String> addService(@RequestBody ServiceRequest serviceRequest) {
        return adminService.addService(serviceRequest);
    }

    @GetMapping("/get-all-services")
    public ResponseEntity<List<ServicesResponse>> getAllServices() {
        return adminService.getAllServices();
    }

}
