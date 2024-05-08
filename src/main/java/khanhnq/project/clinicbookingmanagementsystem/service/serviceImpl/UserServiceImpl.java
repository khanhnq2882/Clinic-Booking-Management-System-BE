package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
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
    private final CommonServiceImpl commonServiceImpl;

    @Override
    public String updateProfile(UserProfileRequest profileRequest) {
        User currentUser = authService.getCurrentUser();
        commonServiceImpl.updateProfile(profileRequest, currentUser);
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_PROFILE_SUCCESS;
    }

    @Override
    public String uploadAvatar(MultipartFile multipartFile) {
        return commonServiceImpl.uploadFile(multipartFile, "avatar");
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
        Long workScheduleId = bookingAppointmentRequest.getWorkScheduleId();
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId).orElse(null);
        if (Objects.isNull(workSchedule))
            throw new ResourceNotFoundException("Work Schedule ID", workScheduleId.toString());
        bookingRepository.findAll().forEach(booking -> {
            if (workScheduleId.equals(booking.getWorkSchedule().getWorkScheduleId()) && appointmentDate.equals(booking.getAppointmentDate().toString())) {
                throw new BusinessException("You cannot booking an appointment at time " + workSchedule.getStartTime() +"-"+ workSchedule.getEndTime()
                        +" on day "+appointmentDate+" because someone has already booked.");
            }
        });
        Booking bookingAppointment = BookingMapper.BOOKING_MAPPER.mapToBooking(bookingAppointmentRequest);
        Ward ward = wardRepository.findById(bookingAppointmentRequest.getWardId()).orElse(null);
        if (Objects.isNull(ward))
            throw new ResourceNotFoundException("Ward ID", bookingAppointmentRequest.getWardId().toString());
        Address address = Address.builder().specificAddress(bookingAppointmentRequest.getSpecificAddress()).ward(ward).build();
        bookingAppointment.setAddress(address);
        bookingAppointment.setBookingCode(commonServiceImpl.bookingCode());
        bookingAppointment.setWorkSchedule(workSchedule);
        bookingAppointment.setStatus(EBookingStatus.PENDING);
        bookingAppointment.setUser(currentUser);
        bookingAppointment.setCreatedBy(currentUser.getUsername());
        bookingRepository.save(bookingAppointment);
        return MessageConstants.BOOKING_SUCCESS;
    }

}