package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/user", produces = {MediaType.APPLICATION_JSON_VALUE})
public class UserController {
    private final UserService userService;

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody UserProfileRequest userProfileRequest) {
        return MessageResponse.getResponseMessage(userService.updateProfile(userProfileRequest), HttpStatus.OK);
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadFile(@RequestParam("avatar") MultipartFile file) {
        return MessageResponse.getResponseMessage(userService.uploadAvatar(file), HttpStatus.OK);
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

    @GetMapping("/get-bookings")
    public ResponseEntity<BookingResponse> getBookings(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "3") int size,
                                                       @RequestParam(defaultValue = "bookingId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getAllBookings(page, size, sorts));
    }

}