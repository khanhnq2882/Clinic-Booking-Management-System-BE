package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.SystemException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.model.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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
    public ResponseEntityBase updateProfile(UserProfileRequest profileRequest, MultipartFile avatar) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = (User) authService.getCurrentUser().getData();
        commonServiceImpl.updateProfile(profileRequest, currentUser, avatar);
        userRepository.save(currentUser);
        response.setData(MessageConstants.UPDATE_PROFILE_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getDoctorsBySpecialization(Long specializationId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<DoctorInfoDTO> doctorInfoList = doctorRepository.getDoctorsBySpecialization(specializationId);
        Map<Long, DoctorDTO> doctorMap = new HashMap<>();
        doctorInfoList.forEach(doctorInfoDTO -> {
            Long doctorId = doctorInfoDTO.getDoctorId();
            DoctorDTO doctorDTO = doctorMap.getOrDefault(doctorId, new DoctorDTO());
            doctorDTO.setDoctorId(doctorId);
            doctorDTO.setFirstName(doctorInfoDTO.getFirstName());
            doctorDTO.setLastName(doctorInfoDTO.getLastName());
            doctorDTO.setSpecializationName(doctorInfoDTO.getSpecializationName());
            doctorDTO.setEducationLevel(doctorInfoDTO.getEducationLevel());
            doctorDTO.setBiography(doctorInfoDTO.getBiography());
            if (doctorInfoDTO.getFileType().equals("avatar")) {
                String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(doctorInfoDTO.getFileId().toString()).toUriString();
                FileResponse fileResponse = new FileResponse(doctorInfoDTO.getFileType(), doctorInfoDTO.getFileName(), fileUrl);
                doctorDTO.setFile(fileResponse);
            }
            if (doctorInfoDTO.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDTO.getDaysOfWeek();
                getDayOfWeekDetails(doctorInfoDTO, daysOfWeek);
            }
            doctorMap.put(doctorId, doctorDTO);
        });
        response.setData(doctorMap.values().stream().toList());
        return response;
    }

    @Override
    public ResponseEntityBase getDoctorDetails(Long doctorId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<DoctorInfoDTO> doctorDetails = doctorRepository.getDoctorDetails(doctorId);
        Map<Long, DoctorDetailsDTO> doctorMap = new HashMap<>();
        doctorDetails.forEach(doctorInfoDTO -> {
            DoctorDetailsDTO doctorDetailsDTO = doctorMap.getOrDefault(doctorId, new DoctorDetailsDTO());
            doctorDetailsDTO.setDoctorId(doctorId);
            doctorDetailsDTO.setUserCode(doctorInfoDTO.getUserCode());
            doctorDetailsDTO.setFirstName(doctorInfoDTO.getFirstName());
            doctorDetailsDTO.setLastName(doctorInfoDTO.getLastName());
            doctorDetailsDTO.setSpecializationName(doctorInfoDTO.getSpecializationName());
            doctorDetailsDTO.setBiography(doctorInfoDTO.getBiography());
            doctorDetailsDTO.setCareerDescription(doctorInfoDTO.getCareerDescription());
            doctorDetailsDTO.setEducationLevel(doctorInfoDTO.getEducationLevel());
            if (doctorInfoDTO.getPosition() != null ||
                    doctorInfoDTO.getWorkSpecializationName() != null ||
                    doctorInfoDTO.getWorkPlace() != null ||
                    doctorInfoDTO.getYearOfStartWork() != null ||
                    doctorInfoDTO.getYearOfEndWork() != null ||
                    doctorInfoDTO.getDescription() != null) {
                WorkExperienceDTO workExperienceDTO = WorkExperienceDTO.builder()
                        .position(doctorInfoDTO.getPosition())
                        .workSpecializationName(doctorInfoDTO.getWorkSpecializationName())
                        .workPlace(doctorInfoDTO.getWorkPlace())
                        .yearOfStartWork(doctorInfoDTO.getYearOfStartWork())
                        .yearOfEndWork(doctorInfoDTO.getYearOfEndWork())
                        .description(doctorInfoDTO.getDescription())
                        .build();
                doctorDetailsDTO.getWorkExperiences().add(workExperienceDTO);
            }
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(doctorInfoDTO.getFileId().toString()).toUriString();
            FileResponse fileResponse = new FileResponse(doctorInfoDTO.getFileType(), doctorInfoDTO.getFileName(), fileUrl);
            doctorDetailsDTO.getFiles().add(fileResponse);
            if (doctorInfoDTO.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDetailsDTO.getDaysOfWeek();
                getDayOfWeekDetails(doctorInfoDTO, daysOfWeek);
            }
            doctorMap.put(doctorId, doctorDetailsDTO);
        });
        response.setData(doctorMap.get(doctorId));
        return response;
    }

    @Override
    public ResponseEntityBase bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = (User) authService.getCurrentUser().getData();
        Date appointmentDate = bookingAppointmentRequest.getAppointmentDate();
        LocalDate workingDayLocalDate = appointmentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate nowLocalDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        if (workingDayLocalDate.isBefore(nowLocalDate) ||
                workingDayLocalDate.isBefore(startOfWeek) ||
                workingDayLocalDate.isAfter(endOfWeek)) {
            throw new SystemException(MessageConstants.INVALID_APPOINTMENT_DATE);
        }
        Long workScheduleId = bookingAppointmentRequest.getWorkScheduleId();
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId).orElseThrow(()
                -> new ResourceNotFoundException("Work schedule id", workScheduleId.toString()));
        bookingRepository.getBookingsWithoutCancelledStatus().forEach(booking -> {
            if (workScheduleId.equals(booking.getWorkSchedule().getWorkScheduleId())) {
                String formatAppointmentDate = new SimpleDateFormat("dd-MM-yyyy").format(appointmentDate);
                String workScheduleTime = workSchedule.getStartTime() +"-"+ workSchedule.getEndTime();
                throw new SystemException("You cannot booking an appointment at time " + workScheduleTime
                        + " on day "+formatAppointmentDate+" because someone has already booked.");
            }
        });
        Ward ward = wardRepository.findById(bookingAppointmentRequest.getWardId()).orElseThrow(
                () -> new ResourceNotFoundException("Ward id", bookingAppointmentRequest.getWardId().toString()));
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
        response.setData(MessageConstants.BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getAllBookings(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = (User) authService.getCurrentUser().getData();
        Page<Booking> bookingPage = bookingRepository.getBookingsWithUserId(currentUser.getUserId(), commonServiceImpl.pagingSort(page, size, sorts));
        response.setData(commonServiceImpl.getAllBookings(bookingPage));
        return response;
    }

    private void getDayOfWeekDetails(DoctorInfoDTO doctorInfoDTO, Set<DayOfWeekDTO> daysOfWeek) {
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
}