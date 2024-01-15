package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import khanhnq.project.clinicbookingmanagementsystem.service.common.MethodsCommon;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final BookingRepository bookingRepository;
    private final AuthService authService;
    private final MethodsCommon methodsCommon;

    @Override
    public String updateProfile(UserProfileRequest profileRequest) {
        User currentUser = authService.getCurrentUser();
        methodsCommon.updateProfile(profileRequest, currentUser);
        userRepository.save(currentUser);
        return "Update profile successfully.";
    }

    @Override
    public String uploadAvatar(MultipartFile multipartFile) {
        return methodsCommon.uploadFile(multipartFile, "avatar");
    }

    @Override
    public List<DoctorDTO> getDoctorsBySpecialization(Long specializationId) {
        return userRepository.getDoctorsBySpecializationId(specializationId)
                .stream()
                .map(user -> DoctorDTO.builder()
                        .userId(user.getUserId())
                        .userCode(user.getUserCode())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkScheduleDTO> getWorkSchedulesByDoctor(Long userId) {
        return workScheduleRepository.getWorkSchedulesByUserId(userId)
                .stream()
                .map(workSchedule -> WorkScheduleDTO.builder()
                        .workScheduleId(workSchedule.getWorkScheduleId())
                        .startTime(workSchedule.getStartTime())
                        .endTime(workSchedule.getEndTime())
                        .build())
                .sorted(Comparator.comparing(WorkScheduleDTO::getStartTime))
                .toList();
    }

    @Override
    public String bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        User currentUser = authService.getCurrentUser();
        String appointmentDate = dateFormat.format(bookingAppointmentRequest.getAppointmentDate());
        WorkSchedule workSchedule = workScheduleRepository.findById(bookingAppointmentRequest.getWorkScheduleId()).orElse(null);
        for (Booking booking : bookingRepository.findAll()) {
            if (bookingAppointmentRequest.getWorkScheduleId().equals(booking.getWorkSchedule().getWorkScheduleId())
            && appointmentDate.equals(booking.getAppointmentDate().toString())) {
                throw new ResourceException("You cannot schedule an appointment at time "
                        + Objects.requireNonNull(workSchedule).getStartTime() +" - "+ workSchedule.getEndTime() +" on day "+appointmentDate, HttpStatus.BAD_REQUEST);
            }
        }
        Booking bookingAppointment = BookingMapper.BOOKING_MAPPER.mapToBooking(bookingAppointmentRequest);
        bookingAppointment.setAddress(Address.builder()
                .specificAddress(bookingAppointmentRequest.getSpecificAddress())
                .ward(wardRepository.findById(bookingAppointmentRequest.getWardId()).orElse(null))
                .build());
        bookingAppointment.setBookingCode(methodsCommon.bookingCode());
        bookingAppointment.setWorkSchedule(workSchedule);
        bookingAppointment.setStatus(EBookingStatus.PENDING);
        bookingAppointment.setUser(currentUser);
        bookingRepository.save(bookingAppointment);
        return "Booking appointment successfully.";
    }

}