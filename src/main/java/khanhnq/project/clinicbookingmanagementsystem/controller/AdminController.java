package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@AllArgsConstructor
@RequestMapping("/admin")

public class AdminController {
    private final AdminService adminService;
    private final FileService fileService;

    @PostMapping("/approve-request-doctor/{userId}")
    public ResponseEntity<String> approveRequestDoctor(@PathVariable("userId") Long userId) {
        return adminService.approveRequestDoctor(userId);
    }

    @PostMapping("/reject-request-doctor/{userId}")
    public ResponseEntity<String> rejectRequestDoctor(@PathVariable("userId") Long userId) {
        return adminService.rejectRequestDoctor(userId);
    }

    @GetMapping("/get-all-users")
    public ResponseEntity<UserPageResponse> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "3") int size,
                                                        @RequestParam(defaultValue = "userId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllUsers(page, size, sort));
    }

    @GetMapping("/get-all-request-doctors")
    public ResponseEntity<List<RequestDoctorResponse>> getAllRequestDoctors() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllRequestDoctors());
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
