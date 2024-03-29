package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService {
    String updateProfile(UserProfileRequest userProfileRequest);
    String uploadAvatar(MultipartFile file);
    List<DoctorDTO> getDoctorsBySpecialization(Long specializationId);
    List<WorkScheduleDTO> getWorkSchedulesByDoctor(Long userId);
    String bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest);
}