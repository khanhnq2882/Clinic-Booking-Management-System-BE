package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import java.util.List;

public interface DoctorService {
    String addDoctorInformation(DoctorInformationRequest doctorInformationRequest);
    List<BookingDTO> getAllUserBookings();
    String changeBookingStatus (Long bookingId, String status);
}
