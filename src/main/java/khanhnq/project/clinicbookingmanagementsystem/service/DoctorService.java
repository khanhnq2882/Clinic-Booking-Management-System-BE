package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;

import java.util.List;

public interface DoctorService {
    String addDoctorInformation(DoctorInformationRequest doctorInformationRequest);
    String confirmedBooking (Long bookingId);
    String cancelledBooking (Long bookingId);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}
