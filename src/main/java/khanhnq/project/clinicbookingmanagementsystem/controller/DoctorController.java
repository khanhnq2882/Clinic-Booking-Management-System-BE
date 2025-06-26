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
import java.util.List;

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

    @GetMapping("/doctor-profile")
    public ResponseEntity<ResponseEntityBase> getUserProfile() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getUserProfile());
    }

    @GetMapping("/doctor-career-info")
    public ResponseEntity<ResponseEntityBase> getDoctorCareerInfo() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getDoctorCareerInfo());
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

    @GetMapping("/booking-detail/{bookingId}")
    public ResponseEntity<ResponseEntityBase> getUserProfile(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getBookingDetail(bookingId));
    }

    @PostMapping("/add-medical-record/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> addMedicalRecord(@PathVariable("bookingId") Long bookingId,
                                                               @RequestBody MedicalRecordRequest medicalRecordRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.addMedicalRecord(bookingId, medicalRecordRequest));
    }

    @PostMapping("/update-medical-record/{medicalRecordId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> updateMedicalRecord(@PathVariable("medicalRecordId") Long medicalRecordId,
                                                               @RequestBody MedicalRecordRequest medicalRecordRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.updateMedicalRecord(medicalRecordId, medicalRecordRequest));
    }

    @PostMapping("/add-lab-results-to-medical-record")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> addLabResultsToMedicalRecord(@RequestBody List<LabResultRequest> labResultRequests) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.addLabResultsToMedicalRecord(labResultRequests));
    }

    @GetMapping("/get-all-medical-records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> getAllMedicalRecords() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getAllMedicalRecords());
    }

    @GetMapping("/get-medical-record-by-booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> getMedicalRecordByBookingId(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getMedicalRecordByBookingId(bookingId));
    }

    @PostMapping(value = "/add-medical-images-to-medical-record", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseEntityBase> addMedicalImagesToMedicalRecord(@RequestPart("medicalImageRequest") MedicalImageRequest medicalImageRequest,
                                                            @Valid @RequestPart(value = "medicalImages") List<MultipartFile> medicalImages) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.addMedicalImagesToMedicalRecord(medicalImageRequest, medicalImages));
    }
}
