package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@AllArgsConstructor
@RequestMapping("/doctor")
public class DoctorController {
    private final DoctorService doctorService;
    @PostMapping("/add-doctor-information")
    public ResponseEntity<String> addDoctorInformation(@RequestBody DoctorInformationRequest doctorInformationRequest) {
        return MessageResponse.getResponseMessage(doctorService.addDoctorInformation(doctorInformationRequest), HttpStatus.OK);
    }

    @GetMapping("/get-all-user-bookings")
    public ResponseEntity<List<BookingDTO>> getAllUserBookings() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getAllUserBookings());
    }

}
