package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService {
    String updateProfile(UserProfileRequest userProfileRequest, MultipartFile multipartFile);
    List<DoctorDTO> getDoctorsBySpecialization(Long specializationId);
    List<WorkScheduleDTO> getWorkSchedulesByDoctor(Long userId);
    String bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}