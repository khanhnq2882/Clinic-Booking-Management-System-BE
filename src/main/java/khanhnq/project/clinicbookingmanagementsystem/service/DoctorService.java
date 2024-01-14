package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import java.util.List;

public interface DoctorService {
    String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest);
    String registerWorkSchedules(List<RegisterWorkScheduleRequest> registerWorkScheduleRequests);
    String confirmedBooking (Long bookingId);
    String cancelledBooking (Long bookingId);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}
