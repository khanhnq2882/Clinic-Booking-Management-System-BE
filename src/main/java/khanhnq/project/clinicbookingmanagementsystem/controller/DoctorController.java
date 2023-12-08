package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@AllArgsConstructor
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/update-doctor-information")
    public ResponseEntity<String> addDoctorInformation(@RequestBody DoctorInformationRequest doctorInformationRequest) {
        return MessageResponse.getResponseMessage(doctorService.updateDoctorInformation(doctorInformationRequest), HttpStatus.OK);
    }

    @GetMapping("/get-all-user-bookings")
    public ResponseEntity<BookingResponse> getAllUserBookings(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "3") int size,
                                                              @RequestParam(defaultValue = "bookingId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(doctorService.getAllBookings(page, size, sort));
    }

    // confirmed booking
    @PostMapping("/confirmed-booking/{bookingId}")
    public ResponseEntity<String> confirmedBooking(@PathVariable("bookingId") Long bookingId) {
        return MessageResponse.getResponseMessage(doctorService.confirmedBooking(bookingId), HttpStatus.OK);
    }

    // cancelled booking
    @PostMapping("/cancelled-booking/{bookingId}")
    public ResponseEntity<String> cancelledBooking(@PathVariable("bookingId") Long bookingId) {
        return MessageResponse.getResponseMessage(doctorService.cancelledBooking(bookingId), HttpStatus.OK);
    }

}
