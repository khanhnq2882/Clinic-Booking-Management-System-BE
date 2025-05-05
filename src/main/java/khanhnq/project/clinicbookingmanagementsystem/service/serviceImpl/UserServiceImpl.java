package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final BookingRepository bookingRepository;
    private final DoctorRepository doctorRepository;
    private final AuthService authService;
    private final CommonServiceImpl commonServiceImpl;

    @Override
    public String updateProfile(UserProfileRequest profileRequest, MultipartFile avatar) {
        User currentUser = authService.getCurrentUser();
        commonServiceImpl.updateProfile(profileRequest, currentUser, avatar);
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_PROFILE_SUCCESS;
    }

    @Override
    public List<DoctorDTO> getDoctorsBySpecialization(Long specializationId) {
        List<DoctorInfoDTO> doctorInfoList = doctorRepository.getDoctorsInfo(specializationId);
        Map<Long, DoctorDTO> doctorMap = new HashMap<>();
        doctorInfoList.forEach(doctorInfoDTO -> {
            Long doctorId = doctorInfoDTO.getDoctorId();
            DoctorDTO doctorDTO = doctorMap.getOrDefault(doctorId, new DoctorDTO());
            doctorDTO.setDoctorId(doctorId);
            doctorDTO.setUserCode(doctorInfoDTO.getUserCode());
            doctorDTO.setFirstName(doctorInfoDTO.getFirstName());
            doctorDTO.setLastName(doctorInfoDTO.getLastName());
            doctorDTO.setSpecializationName(doctorInfoDTO.getSpecializationName());
            doctorDTO.setBiography(doctorInfoDTO.getBiography());
            doctorDTO.setCareerDescription(doctorInfoDTO.getCareerDescription());
            doctorDTO.setEducationLevel(doctorInfoDTO.getEducationLevel());
            WorkExperienceDTO workExperienceDTO = WorkExperienceDTO.builder()
                    .position(doctorInfoDTO.getPosition())
                    .workSpecializationName(doctorInfoDTO.getWorkSpecializationName())
                    .workPlace(doctorInfoDTO.getWorkPlace())
                    .yearOfStartWork(doctorInfoDTO.getYearOfStartWork())
                    .yearOfEndWork(doctorInfoDTO.getYearOfEndWork())
                    .description(doctorInfoDTO.getDescription())
                    .build();
            doctorDTO.getWorkExperiences().add(workExperienceDTO);
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(doctorInfoDTO.getFileId().toString()).toUriString();
            FileResponse fileResponse = new FileResponse(doctorInfoDTO.getFileType(), doctorInfoDTO.getFileName(), fileUrl);
            doctorDTO.getFiles().add(fileResponse);
            if (doctorInfoDTO.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDTO.getDaysOfWeek();
                DayOfWeekDTO existingDay = daysOfWeek.stream()
                        .filter(d -> d.getWorkingDay().equals(doctorInfoDTO.getWorkingDay()))
                        .findFirst()
                        .orElse(null);
                WorkScheduleDTO workScheduleDTO = new WorkScheduleDTO();
                workScheduleDTO.setStartTime(doctorInfoDTO.getStartTime());
                workScheduleDTO.setEndTime(doctorInfoDTO.getEndTime());
                if (existingDay != null) {
                    existingDay.getWorkSchedules().add(workScheduleDTO);
                    Set<WorkScheduleDTO> sorted = existingDay.getWorkSchedules().stream()
                            .sorted(Comparator.comparing(WorkScheduleDTO::getStartTime))
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    existingDay.setWorkSchedules(sorted);
                } else {
                    DayOfWeekDTO newDay = new DayOfWeekDTO();
                    newDay.setWorkingDay(doctorInfoDTO.getWorkingDay());
                    Set<WorkScheduleDTO> sorted = Stream.of(workScheduleDTO)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    newDay.setWorkSchedules(sorted);
                    daysOfWeek.add(newDay);
                }
            }
            doctorMap.put(doctorId, doctorDTO);
        });
        return doctorMap.values().stream().toList();
    }

    @Override
    public String bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest) {
        User currentUser = authService.getCurrentUser();
        LocalDate ld1 = bookingAppointmentRequest.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ld2 = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ld1.isBefore(ld2)) {
            throw new BusinessException(MessageConstants.INVALID_APPOINTMENT_DATE);
        }
        String appointmentDate = new SimpleDateFormat("yyyy-MM-dd").format(bookingAppointmentRequest.getAppointmentDate());
        Long workScheduleId = bookingAppointmentRequest.getWorkScheduleId();
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId).orElseThrow(()
                -> new ResourceNotFoundException("Work schedule id", workScheduleId.toString()));
        bookingRepository.findAll().forEach(booking -> {
            if (workScheduleId.equals(booking.getWorkSchedule().getWorkScheduleId())
                    && appointmentDate.equals(booking.getAppointmentDate().toString())) {
                String workScheduleTime = workSchedule.getStartTime() +"-"+ workSchedule.getEndTime();
                throw new BusinessException("You cannot booking an appointment at time " + workScheduleTime
                        + " on day "+appointmentDate+" because someone has already booked.");
            }
        });
        Ward ward = wardRepository.findById(bookingAppointmentRequest.getWardId()).orElseThrow(()
                -> new ResourceNotFoundException("Ward id", bookingAppointmentRequest.getWardId().toString()));
        Address address = Address.builder().specificAddress(bookingAppointmentRequest.getSpecificAddress()).ward(ward).build();
        Booking bookingAppointment = BookingMapper.BOOKING_MAPPER.mapToBooking(bookingAppointmentRequest);
        bookingAppointment.setAddress(address);
        bookingAppointment.setBookingCode(commonServiceImpl.bookingCode());
        bookingAppointment.setWorkSchedule(workSchedule);
        bookingAppointment.setStatus(EBookingStatus.PENDING);
        bookingAppointment.setUser(currentUser);
        bookingAppointment.setCreatedBy(currentUser.getUsername());
        bookingAppointment.setCreatedAt(LocalDateTime.now());
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