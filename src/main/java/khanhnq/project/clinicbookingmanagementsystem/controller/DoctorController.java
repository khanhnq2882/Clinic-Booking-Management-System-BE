package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.model.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/doctor", produces = {MediaType.APPLICATION_JSON_VALUE})
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseEntityBase> updateProfile(@RequestPart("userProfileRequest") UserProfileRequest userProfileRequest,
                                                            @Valid @RequestPart(value = "avatar") MultipartFile avatar) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.updateProfile(userProfileRequest, avatar));
    }

    @PostMapping("/update-doctor-information")
    public ResponseEntity<ResponseEntityBase> updateDoctorInformation(@RequestPart DoctorInformationRequest doctorInformationRequest,
                                                          @Valid @RequestPart(value = "specialty-degree", required = false) MultipartFile specialtyDegree) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.updateDoctorInformation(doctorInformationRequest, specialtyDegree));
    }

    @PostMapping("/register-work-schedules")
    public ResponseEntity<ResponseEntityBase> registerWorkSchedules(@RequestBody RegisterWorkScheduleRequest registerWorkScheduleRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.registerWorkSchedules(registerWorkScheduleRequest));
    }

    @GetMapping("/get-all-user-bookings")
    public ResponseEntity<ResponseEntityBase> getAllUserBookings(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "3") int size,
                                                              @RequestParam(defaultValue = "bookingId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getAllBookings(page, size, sort));
    }

    @PostMapping("/confirmed-booking/{bookingId}")
    public ResponseEntity<ResponseEntityBase> confirmedBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.confirmedBooking(bookingId));
    }

    @PostMapping("/cancelled-booking/{bookingId}")
    public ResponseEntity<ResponseEntityBase> cancelledBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.cancelledBooking(bookingId));
    }

    @PostMapping("/completed-booking/{bookingId}")
    public ResponseEntity<ResponseEntityBase> completedBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.completedBooking(bookingId));
    }

}
