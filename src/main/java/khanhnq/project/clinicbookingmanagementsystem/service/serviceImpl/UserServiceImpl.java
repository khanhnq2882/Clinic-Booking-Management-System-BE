package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.exception.ForbiddenException;
import khanhnq.project.clinicbookingmanagementsystem.exception.UnauthorizedException;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
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
        User currentUser = checkAccess();
        commonServiceImpl.updateProfile(profileRequest, currentUser, avatar);
        userRepository.save(currentUser);
        response.setData(MessageConstants.UPDATE_PROFILE_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getDoctorsBySpecialization(Long specializationId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<DoctorInfo> doctorInfoList = doctorRepository.getDoctorsBySpecialization(specializationId);
        Map<Long, DoctorDTO> doctorMap = new HashMap<>();
        doctorInfoList.forEach(doctorInfo -> {
            Long doctorId = doctorInfo.getDoctorId();
            DoctorDTO doctorDTO = doctorMap.getOrDefault(doctorId, new DoctorDTO());
            doctorDTO.setDoctorId(doctorId);
            doctorDTO.setFirstName(doctorInfo.getFirstName());
            doctorDTO.setLastName(doctorInfo.getLastName());
            doctorDTO.setSpecializationName(doctorInfo.getSpecializationName());
            doctorDTO.setEducationLevel(doctorInfo.getEducationLevel());
            doctorDTO.setBiography(doctorInfo.getBiography());
            if (doctorInfo.getFileType().equals("avatar")) {
                String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(doctorInfo.getFileId().toString()).toUriString();
                FileResponse fileResponse = new FileResponse(doctorInfo.getFileType(), doctorInfo.getFileName(), fileUrl);
                doctorDTO.setFile(fileResponse);
            }
            if (doctorInfo.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDTO.getDaysOfWeek();
                getDayOfWeekDetails(doctorInfo, daysOfWeek);
            }
            doctorMap.put(doctorId, doctorDTO);
        });
        response.setData(doctorMap.values().stream().toList());
        return response;
    }

    @Override
    public ResponseEntityBase getDoctorDetails(Long doctorId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<DoctorInfo> doctorDetails = doctorRepository.getDoctorDetails(doctorId);
        Map<Long, DoctorDetailsDTO> doctorMap = new HashMap<>();
        doctorDetails.forEach(doctorInfo -> {
            DoctorDetailsDTO doctorDetailsDTO = doctorMap.getOrDefault(doctorId, new DoctorDetailsDTO());
            doctorDetailsDTO.setDoctorId(doctorId);
            doctorDetailsDTO.setUserCode(doctorInfo.getUserCode());
            doctorDetailsDTO.setFirstName(doctorInfo.getFirstName());
            doctorDetailsDTO.setLastName(doctorInfo.getLastName());
            doctorDetailsDTO.setSpecializationName(doctorInfo.getSpecializationName());
            doctorDetailsDTO.setBiography(doctorInfo.getBiography());
            doctorDetailsDTO.setCareerDescription(doctorInfo.getCareerDescription());
            doctorDetailsDTO.setEducationLevel(doctorInfo.getEducationLevel());
            if (doctorInfo.getPosition() != null || doctorInfo.getWorkSpecializationName() != null ||
                    doctorInfo.getWorkPlace() != null || doctorInfo.getYearOfStartWork() != null ||
                    doctorInfo.getYearOfEndWork() != null || doctorInfo.getDescription() != null) {
                WorkExperienceDTO workExperienceDTO = WorkExperienceDTO.builder()
                        .position(doctorInfo.getPosition())
                        .workSpecializationName(doctorInfo.getWorkSpecializationName())
                        .workPlace(doctorInfo.getWorkPlace())
                        .yearOfStartWork(doctorInfo.getYearOfStartWork())
                        .yearOfEndWork(doctorInfo.getYearOfEndWork())
                        .description(doctorInfo.getDescription())
                        .build();
                doctorDetailsDTO.getWorkExperiences().add(workExperienceDTO);
            }
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(doctorInfo.getFileId().toString()).toUriString();
            FileResponse fileResponse = new FileResponse(doctorInfo.getFileType(), doctorInfo.getFileName(), fileUrl);
            doctorDetailsDTO.getFiles().add(fileResponse);
            if (doctorInfo.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDetailsDTO.getDaysOfWeek();
                getDayOfWeekDetails(doctorInfo, daysOfWeek);
            }
            doctorMap.put(doctorId, doctorDetailsDTO);
        });
        response.setData(doctorMap.get(doctorId));
        return response;
    }

    @Override
    public ResponseEntityBase bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = checkAccess();
        Long workScheduleId = bookingAppointmentRequest.getWorkScheduleId();
        WorkSchedule workSchedule = validateWorkSchedule(workScheduleId);
        Address address = createAddress(bookingAppointmentRequest.getWardId(), bookingAppointmentRequest.getSpecificAddress());
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
    public ResponseEntityBase updateBookedAppointment(Long bookingId, BookingAppointmentRequest bookingAppointmentRequest) {
        User currentUser = checkAccess();
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new ResourceNotFoundException("Booking id", bookingId.toString());
        }
        Booking booking = bookingOptional.get();
        String bookingStatus = booking.getStatus().toString();
        if (!bookingStatus.equals("PENDING")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be updated. Contact to admin.");
        }
        Long workScheduleId = bookingAppointmentRequest.getWorkScheduleId();
        WorkSchedule workSchedule = validateWorkSchedule(workScheduleId);
        Address address = createAddress(bookingAppointmentRequest.getWardId(), bookingAppointmentRequest.getSpecificAddress());
        BookingMapper.BOOKING_MAPPER.updateBooking(booking, bookingAppointmentRequest);
        booking.setAddress(address);
        booking.setWorkSchedule(workSchedule);
        booking.setStatus(EBookingStatus.PENDING);
        booking.setUser(currentUser);
        booking.setUpdatedBy(currentUser.getUsername());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        response.setData(MessageConstants.UPDATE_BOOKED_APPOINTMENT_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase cancelAppointment(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = checkAccess();
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new ResourceNotFoundException("Booking id", bookingId.toString());
        }
        Booking booking = bookingOptional.get();
        if (!booking.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException(MessageConstants.FORBIDDEN_ACCESS);
        }
        WorkSchedule workSchedule = booking.getWorkSchedule();
        if (workSchedule != null && workScheduleRepository.findById(workSchedule.getWorkScheduleId()).isEmpty()) {
            throw new ResourceNotFoundException("Work schedule id", workSchedule.getWorkScheduleId().toString());
        }
        String bookingStatus = booking.getStatus().toString();
        if (bookingStatus.equals("CONFIRMED") || bookingStatus.equals("COMPLETED") || bookingStatus.equals("CANCELLED")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be cancelled.");
        }
        BookingInfo bookingInfo = bookingRepository.getBookingInfoByBookingId(bookingId);
        if (bookingInfo != null) {
            try {
                Date workingDayDateFormat = new SimpleDateFormat("yyyy-MM-dd").parse(bookingInfo.getWorkingDay().toString().substring(0, 10));
                LocalDate workingDay = workingDayDateFormat.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDateTime workingDayDateTime = LocalDateTime.of(workingDay, bookingInfo.getStartTime());
                if (LocalDateTime.now().isAfter(workingDayDateTime.minusHours(24)) &&
                        Duration.between(bookingInfo.getCreatedAt(), LocalDateTime.now()).toMinutes() > 60) {
                    throw new SystemException(MessageConstants.CANCELLATION_APPOINTMENT_TIME_ERROR);
                }
            } catch (ParseException exception) {
                throw new SystemException(MessageConstants.SOMETHING_WENT_WRONG);
            }
        }
        booking.setStatus(EBookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setUpdatedBy(currentUser.getUsername());
        bookingRepository.save(booking);
        response.setData(MessageConstants.CANCEL_APPOINTMENT_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getAllBookings(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = checkAccess();
        Page<Booking> bookingPage = bookingRepository.getBookingsWithUserId(currentUser.getUserId(), commonServiceImpl.pagingSort(page, size, sorts));
        response.setData(commonServiceImpl.getAllBookings(bookingPage));
        return response;
    }

    public User checkAccess() {
        User currentUser = authService.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_USER"))) {
            throw new ForbiddenException(MessageConstants.FORBIDDEN_ACCESS);
        }
        return currentUser;
    }

    private void getDayOfWeekDetails(DoctorInfo doctorInfo, Set<DayOfWeekDTO> daysOfWeek) {
        DayOfWeekDTO existingDay = daysOfWeek.stream()
                .filter(d -> d.getWorkingDay().equals(doctorInfo.getWorkingDay()))
                .findFirst()
                .orElse(null);
        WorkScheduleDTO workScheduleDTO = new WorkScheduleDTO();
        workScheduleDTO.setStartTime(doctorInfo.getStartTime());
        workScheduleDTO.setEndTime(doctorInfo.getEndTime());
        if (existingDay != null) {
            existingDay.getWorkSchedules().add(workScheduleDTO);
            Set<WorkScheduleDTO> sorted = existingDay.getWorkSchedules().stream()
                    .sorted(Comparator.comparing(WorkScheduleDTO::getStartTime))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            existingDay.setWorkSchedules(sorted);
        } else {
            DayOfWeekDTO newDay = new DayOfWeekDTO();
            newDay.setWorkingDay(doctorInfo.getWorkingDay());
            Set<WorkScheduleDTO> sorted = Stream.of(workScheduleDTO)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            newDay.setWorkSchedules(sorted);
            daysOfWeek.add(newDay);
        }
    }

    private WorkSchedule validateWorkSchedule(Long workScheduleId) {
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId).orElseThrow(()
                -> new ResourceNotFoundException("Work schedule id", workScheduleId.toString()));
        List<BookingInfo> bookingInfos = bookingRepository.getBookingsByWorkScheduleId(workScheduleId);
        if (bookingInfos.size() > 0) {
            BookingInfo bookingInfo = bookingInfos.get(0);
            String formatAppointmentDate = new SimpleDateFormat("dd-MM-yyyy").format(bookingInfo.getWorkingDay());
            String workScheduleTime = bookingInfo.getStartTime() + "-" + bookingInfo.getEndTime();
            throw new SystemException("You cannot booking an appointment at time " + workScheduleTime
                    + " on day " + formatAppointmentDate + " because someone has already booked.");
        }
        return workSchedule;
    }

    private Address createAddress(Long wardId, String specificAddress) {
        Ward ward = wardRepository.findById(wardId).orElseThrow(
                () -> new ResourceNotFoundException("Ward id", wardId.toString()));
        return Address.builder().specificAddress(specificAddress).ward(ward).build();
    }

}