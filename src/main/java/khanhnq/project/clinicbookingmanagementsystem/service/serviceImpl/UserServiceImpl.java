package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.model.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
    public String updateProfile(UserProfileRequest profileRequest, MultipartFile file) {
        User currentUser = authService.getCurrentUser();
        commonServiceImpl.updateProfile(profileRequest, currentUser, file);
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_PROFILE_SUCCESS;
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
        return workScheduleRepository.getWorkSchedulesByDoctorId(userId)
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
        LocalDate ld1 = bookingAppointmentRequest.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ld2 = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ld1.isBefore(ld2)) {
            throw new BusinessException(MessageConstants.INVALID_APPOINTMENT_DATE);
        }
        String appointmentDate = dateFormat.format(bookingAppointmentRequest.getAppointmentDate());
        Long workScheduleId = bookingAppointmentRequest.getWorkScheduleId();
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId).orElseThrow(() -> new ResourceNotFoundException("Work schedule ID", workScheduleId.toString()));
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

    @Override
    public BookingResponse getAllBookings(int page, int size, String[] sorts) {
        User currentUser = authService.getCurrentUser();
        Page<Booking> bookingPage = bookingRepository.getBookingsWithUserId(currentUser.getUserId(), commonServiceImpl.pagingSort(page, size, sorts));
        return commonServiceImpl.getAllBookings(bookingPage);
    }

}