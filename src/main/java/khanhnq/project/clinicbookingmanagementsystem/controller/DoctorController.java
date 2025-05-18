package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.validation.Valid;
import khanhnq.project.clinicbookingmanagementsystem.model.request.*;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/doctor", produces = {MediaType.APPLICATION_JSON_VALUE})
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> updateProfile(@RequestPart("userProfileRequest") UserProfileRequest userProfileRequest,
                                                            @Valid @RequestPart(value = "avatar") MultipartFile avatar) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.updateProfile(userProfileRequest, avatar));
    }

    @PostMapping("/update-doctor-information")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> updateDoctorInformation(@RequestPart DoctorInformationRequest doctorInformationRequest,
                                                          @Valid @RequestPart(value = "specialty-degree", required = false) MultipartFile specialtyDegree) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.updateDoctorInformation(doctorInformationRequest, specialtyDegree));
    }

    @PostMapping("/register-work-schedules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> registerWorkSchedules(@RequestBody RegisterWorkScheduleRequest registerWorkScheduleRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.registerWorkSchedules(registerWorkScheduleRequest));
    }

    @GetMapping("/get-all-user-bookings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> getAllUserBookings(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "3") int size,
                                                              @RequestParam(defaultValue = "bookingId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getAllBookings(page, size, sort));
    }

    @PostMapping("/confirmed-booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> confirmedBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.confirmedBooking(bookingId));
    }

    @PostMapping("/cancelled-booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> cancelledBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.cancelledBooking(bookingId));
    }

    @PostMapping("/completed-booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> completedBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.completedBooking(bookingId));
    }

    @PostMapping("/add-medical-record")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> addMedicalRecord(@RequestBody MedicalRecordRequest medicalRecordRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.addMedicalRecord(medicalRecordRequest));
    }

    @PostMapping("/add-lab-result-to-medical-record")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> addLabResultToMedicalRecord(@RequestBody LabResultRequest labResultRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.addLabResultToMedicalRecord(labResultRequest));
    }
}
