package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.MessageResponse;
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
@RequestMapping(path = "/user")
public class UserController {
    private final UserService userService;

    @PostMapping(value = "/update-profile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> updateProfile(
            @RequestPart(value = "userprofile") UserProfileRequest userProfileRequest,
            @Valid @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return MessageResponse.getResponseMessage(userService.updateProfile(userProfileRequest, avatar), HttpStatus.OK);
    }

    @GetMapping("/get-doctors-by-specialization/{specializationId}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialization (@PathVariable("specializationId") Long specializationId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getDoctorsBySpecialization(specializationId));
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