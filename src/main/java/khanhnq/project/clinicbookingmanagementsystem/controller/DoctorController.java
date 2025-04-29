package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.model.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/doctor", produces = {MediaType.APPLICATION_JSON_VALUE})
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody UserProfileRequest userProfileRequest) {
        return MessageResponse.getResponseMessage(doctorService.updateProfile(userProfileRequest), HttpStatus.OK);
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        return MessageResponse.getResponseMessage(doctorService.uploadAvatar(file), HttpStatus.OK);
    }

    @PostMapping("/update-doctor-information")
    public ResponseEntity<String> updateDoctorInformation(@RequestBody DoctorInformationRequest doctorInformationRequest) {
        return MessageResponse.getResponseMessage(doctorService.updateDoctorInformation(doctorInformationRequest), HttpStatus.OK);
    }

    @PostMapping("/upload-medical-degree")
    public ResponseEntity<String> uploadMedicalDegree(@RequestParam("medical-degree") MultipartFile file) {
        return MessageResponse.getResponseMessage(doctorService.uploadMedicalDegree(file), HttpStatus.OK);
    }

    @PostMapping("/upload-specialty-degree")
    public ResponseEntity<String> uploadSpecialtyDegree(@RequestParam("specialty-degree") MultipartFile file) {
        return MessageResponse.getResponseMessage(doctorService.uploadSpecialtyDegree(file), HttpStatus.OK);
    }

    @PostMapping("/register-work-schedules")
    public ResponseEntity<String> registerWorkSchedules(@RequestBody RegisterWorkScheduleRequest registerWorkScheduleRequest) {
        return MessageResponse.getResponseMessage(doctorService.registerWorkSchedules(registerWorkScheduleRequest), HttpStatus.OK);
    }

    @GetMapping("/get-all-user-bookings")
    public ResponseEntity<BookingResponse> getAllUserBookings(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "3") int size,
                                                              @RequestParam(defaultValue = "bookingId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getAllBookings(page, size, sort));
    }

    @PostMapping("/confirmed-booking/{bookingId}")
    public ResponseEntity<String> confirmedBooking(@PathVariable("bookingId") Long bookingId) {
        return MessageResponse.getResponseMessage(doctorService.confirmedBooking(bookingId), HttpStatus.OK);
    }

    @PostMapping("/cancelled-booking/{bookingId}")
    public ResponseEntity<String> cancelledBooking(@PathVariable("bookingId") Long bookingId) {
        return MessageResponse.getResponseMessage(doctorService.cancelledBooking(bookingId), HttpStatus.OK);
    }

    @PostMapping("/completed-booking/{bookingId}")
    public ResponseEntity<String> completedBooking(@PathVariable("bookingId") Long bookingId) {
        return MessageResponse.getResponseMessage(doctorService.completedBooking(bookingId), HttpStatus.OK);
    }

}
