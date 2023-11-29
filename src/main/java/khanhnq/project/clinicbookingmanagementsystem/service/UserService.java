package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.SkillDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    ResponseEntity<String> updateProfile(UserProfileRequest userProfileRequest);
    ResponseEntity<String> uploadAvatar(MultipartFile file);
    ResponseEntity<String> requestBecomeDoctor(AddRoleDoctorRequest addRoleDoctorRequest);
    ResponseEntity<String> uploadMedicalLicense(MultipartFile multipartFile);
    ResponseEntity<String> uploadMedicalDegree(MultipartFile multipartFile);
    List<SkillDTO> getAllSkills();
    List<DoctorDTO> getDoctorsBySpecialization(Long specializationId);
    List<WorkScheduleDTO> getWorkSchedulesByDoctor(Long userId);
    String bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest);

}