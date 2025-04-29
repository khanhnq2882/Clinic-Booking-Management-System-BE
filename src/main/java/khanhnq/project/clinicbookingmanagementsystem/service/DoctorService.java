package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.model.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DoctorService {
    String updateProfile(UserProfileRequest userProfileRequest);
    String uploadAvatar(MultipartFile file);
    String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest);
    String uploadMedicalDegree(MultipartFile file);
    String uploadSpecialtyDegree(MultipartFile file);
    String registerWorkSchedules(RegisterWorkScheduleRequest registerWorkScheduleRequest);
    String confirmedBooking (Long bookingId);
    String cancelledBooking (Long bookingId);
    String completedBooking (Long bookingId);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}
