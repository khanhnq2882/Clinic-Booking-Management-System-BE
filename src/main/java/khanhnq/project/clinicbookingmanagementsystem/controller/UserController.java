package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.model.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/user")
public class UserController {
    private final UserService userService;

    @PostMapping(value = "/update-profile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> updateProfile(
            @RequestPart(value = "userprofile") UserProfileRequest userProfileRequest,
            @Valid @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.updateProfile(userProfileRequest, avatar));
    }

    @GetMapping("/user-profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> getUserProfile() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getUserProfile());
    }

    @GetMapping("/get-doctors-by-specialization/{specializationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> getDoctorsBySpecialization (@PathVariable("specializationId") Long specializationId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getDoctorsBySpecialization(specializationId));
    }

    @GetMapping("/get-doctor-details/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> getDoctorDetails (@PathVariable("doctorId") Long doctorId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getDoctorDetails(doctorId));
    }

    @PostMapping("/booking-appointment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> bookingAppointment(@RequestBody BookingAppointmentRequest bookingAppointmentRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.bookingAppointment(bookingAppointmentRequest));
    }

    @PostMapping("/booking-appointment-without-account")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> bookingAppointmentWithoutAccount(@RequestBody BookingAppointmentRequest bookingAppointmentRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.bookingAppointmentWithoutAccount(bookingAppointmentRequest));
    }

    @PostMapping("/update-booked-appointment/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> updateBookedAppointment(@PathVariable("bookingId") Long bookingId,
                                                                      @RequestBody BookingAppointmentRequest bookingAppointmentRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.updateBookedAppointment(bookingId, bookingAppointmentRequest));
    }

    @PostMapping("/cancel-appointment/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> cancelAppointment(@PathVariable Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.cancelAppointment(bookingId));
    }

    @GetMapping("/get-bookings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> getBookings(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "3") int size,
                                                       @RequestParam(defaultValue = "bookingId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userService.getAllBookings(page, size, sorts));
    }
}