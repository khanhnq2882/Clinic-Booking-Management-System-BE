package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.SkillDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Specialization;
import khanhnq.project.clinicbookingmanagementsystem.repository.SpecializationRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials="true")
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final SpecializationRepository specializationRepository;

    @GetMapping("/skills")
    public ResponseEntity<List<SkillDTO>> getAllSkills() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getAllSkills());
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(specializationRepository.findAll());
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

    @GetMapping("/get-doctors-by-specialization/{specializationId}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialization (@PathVariable("specializationId") Long specializationId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getDoctorsBySpecialization(specializationId));
    }

    @GetMapping("/get-work-schedules-by-doctor/{userId}")
    public ResponseEntity<List<WorkScheduleDTO>> getWorkSchedulesByDoctor (@PathVariable("userId") Long userId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getWorkSchedulesByDoctor(userId));
    }

    @PostMapping("/booking-appointment")
    public ResponseEntity<String> bookingAppointment(@RequestBody BookingAppointmentRequest bookingAppointmentRequest) {
        return MessageResponse.getResponseMessage(userService.bookingAppointment(bookingAppointmentRequest), HttpStatus.OK);
    }

}