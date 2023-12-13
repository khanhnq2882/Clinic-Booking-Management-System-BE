package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;

public interface DoctorService {
    String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest);
    String confirmedBooking (Long bookingId);
    String cancelledBooking (Long bookingId);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}
