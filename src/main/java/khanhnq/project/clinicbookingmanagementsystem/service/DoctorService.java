package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DoctorService {
    String updateProfile(UserProfileRequest userProfileRequest);
    String uploadAvatar(MultipartFile file);
    String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest);
    String registerWorkSchedules(RegisterWorkScheduleRequest registerWorkScheduleRequest);
    String confirmedBooking (Long bookingId);
    String cancelledBooking (Long bookingId);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}
