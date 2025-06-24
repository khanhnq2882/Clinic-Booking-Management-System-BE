package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.common.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.exception.ForbiddenException;
import khanhnq.project.clinicbookingmanagementsystem.exception.UnauthorizedException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.DoctorMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.SystemException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingTimeInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.model.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntityBase getUserProfile() {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        UserDTO userDTO = commonServiceImpl.getUserDetails(checkAccess());
        response.setData(userDTO);
        return response;
    }

    @Override
    public ResponseEntityBase getDoctorsBySpecialization(Long specializationId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<DoctorDetailsInfoProjection> doctorDetailsInfoProjectionList = doctorRepository.getDoctorsBySpecialization(specializationId);
        Map<Long, DoctorDTO> doctorMap = new HashMap<>();
        doctorDetailsInfoProjectionList.forEach(doctorDetailsInfoProjection -> {
            Long doctorId = doctorDetailsInfoProjection.getDoctorId();
            DoctorDTO doctorDTO = doctorMap.getOrDefault(doctorId, new DoctorDTO());
            DoctorMapper.DOCTOR_MAPPER.mapToDoctorDTO(doctorDTO, doctorDetailsInfoProjection);
            String fileType = doctorDetailsInfoProjection.getFileType();
            if (fileType != null && fileType.equals("avatar")) {
                FileResponse fileResponse = commonServiceImpl.getFileFromS3(
                        fileType,
                        doctorDetailsInfoProjection.getFileName(),
                        doctorDetailsInfoProjection.getFilePath());
                doctorDTO.setFile(fileResponse);
            }
            if (doctorDetailsInfoProjection.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDTO.getDaysOfWeek();
                getDayOfWeekDetails(doctorDetailsInfoProjection, daysOfWeek);
            }
            doctorMap.put(doctorId, doctorDTO);
        });
        response.setData(doctorMap.values().stream().toList());
        return response;
    }

    @Override
    public ResponseEntityBase getDoctorDetails(Long doctorId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<DoctorDetailsInfoProjection> doctorDetails = doctorRepository.getDoctorDetails(doctorId);
        Map<Long, DoctorDetailsDTO> doctorMap = new HashMap<>();
        doctorDetails.forEach(doctorDetailsInfoProjection -> {
            DoctorDetailsDTO doctorDetailsDTO = doctorMap.getOrDefault(doctorId, new DoctorDetailsDTO());
            DoctorMapper.DOCTOR_MAPPER.mapToDoctorDetailsDTO(doctorDetailsDTO, doctorDetailsInfoProjection);
            if (doctorDetailsInfoProjection.getPosition() != null || doctorDetailsInfoProjection.getWorkSpecializationName() != null ||
                    doctorDetailsInfoProjection.getWorkPlace() != null || doctorDetailsInfoProjection.getYearOfStartWork() != null ||
                    doctorDetailsInfoProjection.getYearOfEndWork() != null || doctorDetailsInfoProjection.getDescription() != null) {
                WorkExperienceDTO workExperienceDTO =
                        WorkExperienceMapper.WORK_EXPERIENCE_MAPPER.mapToWorkExperienceDTO(doctorDetailsInfoProjection);
                doctorDetailsDTO.getWorkExperiences().add(workExperienceDTO);
            }
            if (doctorDetailsInfoProjection.getFileId() != null) {
                FileResponse fileResponse = commonServiceImpl.getFileFromS3(
                        doctorDetailsInfoProjection.getFileType(),
                        doctorDetailsInfoProjection.getFileName(),
                        doctorDetailsInfoProjection.getFilePath());
                doctorDetailsDTO.getFiles().add(fileResponse);
            }
            if (doctorDetailsInfoProjection.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDetailsDTO.getDaysOfWeek();
                getDayOfWeekDetails(doctorDetailsInfoProjection, daysOfWeek);
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
    public ResponseEntityBase bookingAppointmentWithoutAccount(BookingAppointmentRequest bookingAppointmentRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Long workScheduleId = bookingAppointmentRequest.getWorkScheduleId();
        WorkSchedule workSchedule = validateWorkSchedule(workScheduleId);
        Address address = createAddress(bookingAppointmentRequest.getWardId(), bookingAppointmentRequest.getSpecificAddress());
        Booking bookingAppointment = BookingMapper.BOOKING_MAPPER.mapToBooking(bookingAppointmentRequest);
        bookingAppointment.setAddress(address);
        bookingAppointment.setBookingCode(commonServiceImpl.bookingCode());
        bookingAppointment.setWorkSchedule(workSchedule);
        bookingAppointment.setStatus(EBookingStatus.PENDING);
        bookingAppointment.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(bookingAppointment);
        response.setData(MessageConstants.BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase updateBookedAppointment(Long bookingId, BookingAppointmentRequest bookingAppointmentRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
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
        BookingTimeInfoProjection bookingTimeInfoProjection = bookingRepository.getBookingInfoByBookingId(bookingId);
        if (bookingTimeInfoProjection != null) {
            try {
                Date workingDayDateFormat = new SimpleDateFormat("yyyy-MM-dd").parse(bookingTimeInfoProjection.getWorkingDay().toString().substring(0, 10));
                LocalDate workingDay = workingDayDateFormat.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDateTime workingDayDateTime = LocalDateTime.of(workingDay, bookingTimeInfoProjection.getStartTime());
                if (LocalDateTime.now().isAfter(workingDayDateTime.minusHours(24)) &&
                        Duration.between(bookingTimeInfoProjection.getCreatedAt(), LocalDateTime.now()).toMinutes() > 60) {
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
        Pageable pageable = commonServiceImpl.pagingSort(page, size, sorts);
        Page<BookingDetailsInfoProjection> bookingDetailsPage = bookingRepository.getBookingDetailsByUserId(currentUser.getUserId(), pageable);
        BookingResponse bookingResponse = BookingResponse.builder()
                .totalItems(bookingDetailsPage.getTotalElements())
                .totalPages(bookingDetailsPage.getTotalPages())
                .currentPage(bookingDetailsPage.getNumber())
                .bookings(bookingDetailsPage.getContent())
                .build();
        response.setData(bookingResponse);
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

    private void getDayOfWeekDetails(DoctorDetailsInfoProjection doctorDetailsInfoProjection, Set<DayOfWeekDTO> daysOfWeek) {
        DayOfWeekDTO existingDay = daysOfWeek.stream()
                .filter(d -> d.getWorkingDay().equals(doctorDetailsInfoProjection.getWorkingDay()))
                .findFirst()
                .orElse(null);
        WorkScheduleDTO workScheduleDTO = new WorkScheduleDTO();
        workScheduleDTO.setStartTime(doctorDetailsInfoProjection.getStartTime());
        workScheduleDTO.setEndTime(doctorDetailsInfoProjection.getEndTime());
        if (existingDay != null) {
            existingDay.getWorkSchedules().add(workScheduleDTO);
            Set<WorkScheduleDTO> sorted = existingDay.getWorkSchedules().stream()
                    .sorted(Comparator.comparing(WorkScheduleDTO::getStartTime))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            existingDay.setWorkSchedules(sorted);
        } else {
            DayOfWeekDTO newDay = new DayOfWeekDTO();
            newDay.setWorkingDay(doctorDetailsInfoProjection.getWorkingDay());
            Set<WorkScheduleDTO> sorted = Stream.of(workScheduleDTO)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            newDay.setWorkSchedules(sorted);
            daysOfWeek.add(newDay);
        }
    }

    private WorkSchedule validateWorkSchedule(Long workScheduleId) {
        WorkSchedule workSchedule = workScheduleRepository.findById(workScheduleId).orElseThrow(()
                -> new ResourceNotFoundException("Work schedule id", workScheduleId.toString()));
        List<BookingTimeInfoProjection> bookingTimeInfoProjections = bookingRepository.getBookingsByWorkScheduleId(workScheduleId);
        if (bookingTimeInfoProjections.size() > 0) {
            BookingTimeInfoProjection bookingTimeInfoProjection = bookingTimeInfoProjections.get(0);
            String formatAppointmentDate = new SimpleDateFormat("dd-MM-yyyy").format(bookingTimeInfoProjection.getWorkingDay());
            String workScheduleTime = bookingTimeInfoProjection.getStartTime() + "-" + bookingTimeInfoProjection.getEndTime();
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