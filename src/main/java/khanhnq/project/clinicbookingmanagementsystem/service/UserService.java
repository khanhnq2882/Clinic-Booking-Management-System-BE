package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService {
    String updateProfile(UserProfileRequest userProfileRequest, MultipartFile multipartFile);
    List<DoctorDTO> getDoctorsBySpecialization(Long specializationId);
    List<WorkScheduleDTO> getWorkSchedulesByDoctor(Long userId);
    String bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}