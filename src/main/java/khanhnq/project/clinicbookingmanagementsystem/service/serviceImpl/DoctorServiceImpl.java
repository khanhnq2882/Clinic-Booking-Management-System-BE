package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.common.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EEducationLevel;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.exception.SystemException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ForbiddenException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkScheduleMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.model.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
    private final SpecializationRepository specializationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final CommonServiceImpl commonServiceImpl;

    @Override
    public ResponseEntityBase updateProfile(UserProfileRequest userProfileRequest, MultipartFile avatar) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        commonServiceImpl.updateProfile(userProfileRequest, currentUser, avatar);
        userRepository.save(currentUser);
        response.setData(MessageConstants.UPDATE_PROFILE_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase updateDoctorInformation(DoctorInformationRequest doctorInformationRequest, MultipartFile specialtyDegree) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Optional<Specialization> specializationOptional =
                specializationRepository.findById(doctorInformationRequest.getSpecializationId());
        if (specializationOptional.isEmpty()) {
            throw new ResourceNotFoundException("Specialization id", doctorInformationRequest.getSpecializationId().toString());
        }
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (Objects.nonNull(doctor)) {
            List<WorkExperience> workExperiences =
                    workExperienceRepository.findWorkExperienceByDoctorId(doctor.getDoctorId());
            workExperienceRepository.deleteAll(workExperiences);
        }
        Doctor savedDoctor = Objects.nonNull(doctor) ? doctor : new Doctor();
        Set<WorkExperience> workExperiences = doctorInformationRequest.getWorkExperiences().stream()
                .map(workExperienceDTO -> {
                    WorkExperience workExperience =
                            WorkExperienceMapper.WORK_EXPERIENCE_MAPPER.mapToExperience(workExperienceDTO);
                    workExperience.setCreatedBy(currentUser.getUsername());
                    workExperience.setCreatedAt(LocalDateTime.now());
                    workExperience.setDoctor(savedDoctor);
                    return workExperience;
                }).collect(Collectors.toSet());
        savedDoctor.setSpecialization(specializationOptional.get());
        savedDoctor.setWorkExperiences(workExperiences);
        savedDoctor.setBiography(doctorInformationRequest.getBiography());
        savedDoctor.setCareerDescription(doctorInformationRequest.getCareerDescription());
        savedDoctor.setEducationLevel(validateEducationLevel(doctorInformationRequest.getEducationLevel()));
        savedDoctor.setUser(currentUser);
        savedDoctor.setUpdatedBy(currentUser.getUsername());
        savedDoctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(savedDoctor);
        if (Objects.nonNull(specialtyDegree)) {
            commonServiceImpl.uploadFile(specialtyDegree, "specialty-degree", currentUser);
        }
        response.setData(MessageConstants.UPDATE_DOCTOR_INFORMATION_SUCCESS);
        return response;
    }

    @Override
    @Transactional
    public ResponseEntityBase registerWorkSchedules(RegisterWorkScheduleRequest registerWorkSchedule) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (Objects.isNull(doctor.getSpecialization())) {
            throw new SystemException(MessageConstants.SPECIALIZATION_NOT_FOUND);
        }
        Date workingDay = registerWorkSchedule.getWorkingDay();
        LocalDate workingDayLocalDate = workingDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate nowLocalDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        if (workingDayLocalDate.isBefore(nowLocalDate) ||
                workingDayLocalDate.isBefore(startOfWeek) ||
                workingDayLocalDate.isAfter(endOfWeek)) {
            throw new SystemException(MessageConstants.INVALID_WORKING_DAY);
        }
        List<WorkScheduleDTO> newWorkSchedules = registerWorkSchedule.getWorkSchedules();
        DaysOfWeek existDayOfWeek = dayOfWeekRepository.getDayOfWeekByWorkingDay(doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
        DaysOfWeek newDayOfWeek = Objects.nonNull(existDayOfWeek) ? existDayOfWeek : new DaysOfWeek();
        Long specializationId = doctor.getSpecialization().getSpecializationId();
        List<Doctor> doctors = doctorRepository.getDoctorsBySpecializationId(specializationId).stream().filter(dt -> !dt.equals(doctor)).toList();
        List<WorkScheduleDTO> invalidWorkSchedules = invalidWorkSchedules(doctors, registerWorkSchedule).stream().toList();
        List<WorkScheduleDTO> filterValidWorkSchedules = newWorkSchedules
                .stream()
                .filter(workScheduleDTO -> !invalidWorkSchedules.contains(workScheduleDTO))
                .toList();
        List<WorkSchedule> workSchedulesNotInBooking = workScheduleRepository.getWorkSchedulesNotInBooking(
                doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
        if (workSchedulesNotInBooking.size() > 0) {
            workScheduleRepository.deleteAll(workSchedulesNotInBooking);
            List<WorkSchedule> workSchedulesInBooking = workScheduleRepository.getWorkSchedulesInBooking(
                    doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
            filterValidWorkSchedules = filterValidWorkSchedules.stream()
                    .filter(workScheduleDTO -> workSchedulesInBooking.stream()
                            .noneMatch(workSchedule ->
                                    workScheduleDTO.getStartTime().equals(workSchedule.getStartTime()) &&
                                            workScheduleDTO.getEndTime().equals(workSchedule.getEndTime())
                            )
                    ).toList();
        }
        List<WorkSchedule> validWorkSchedules = filterValidWorkSchedules
                .stream()
                .map(workScheduleDTO -> {
                        WorkSchedule workSchedule = WorkScheduleMapper.WORK_SCHEDULE_MAPPER.mapToWorkSchedule(workScheduleDTO);
                        workSchedule.setDaysOfWeek(newDayOfWeek);
                        workSchedule.setCreatedBy(currentUser.getUsername());
                        workSchedule.setCreatedAt(LocalDateTime.now());
                        return workSchedule;
                    })
                .toList();
        if (validWorkSchedules.size() == 0) {
            throw new SystemException(MessageConstants.ALL_WORK_SCHEDULES_INVALID);
        } else {
            newDayOfWeek.setWorkingDay(workingDay);
            newDayOfWeek.setDoctor(doctor);
            newDayOfWeek.getWorkSchedules().addAll(validWorkSchedules);
            newDayOfWeek.setWorkSchedules(newDayOfWeek.getWorkSchedules());
            newDayOfWeek.setCreatedBy(currentUser.getUsername());
            newDayOfWeek.setCreatedAt(LocalDateTime.now());
            doctor.getDaysOfWeeks().add(newDayOfWeek);
            doctor.setDaysOfWeeks(doctor.getDaysOfWeeks());
            doctorRepository.save(doctor);
            response.setData(workSchedulesMessage(invalidWorkSchedules, workingDay));
        }
        return response;
    }

    @Override
    public ResponseEntityBase confirmedBooking(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Booking booking = validateChangeBookingStatus(bookingId);
        String bookingStatus = booking.getStatus().toString();
        if (!bookingStatus.equals("PENDING")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be confirmed.");
        }
        booking.setStatus(EBookingStatus.CONFIRMED);
        booking.setUpdatedBy(currentUser.getUsername());
        bookingRepository.save(booking);
        response.setData(MessageConstants.CONFIRM_BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase cancelledBooking(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Booking booking = validateChangeBookingStatus(bookingId);
        String bookingStatus = booking.getStatus().toString();
        if (!bookingStatus.equals("PENDING") && !bookingStatus.equals("CONFIRMED")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be cancelled.");
        }
        booking.setStatus(EBookingStatus.CANCELLED);
        booking.setUpdatedBy(currentUser.getUsername());
        bookingRepository.save(booking);
        response.setData(MessageConstants.CANCELED_BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase completedBooking(Long bookingId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Booking booking = validateChangeBookingStatus(bookingId);
        String bookingStatus = booking.getStatus().toString();
        if (!bookingStatus.equals("CONFIRMED") && !bookingStatus.equals("CANCELLED")) {
            throw new SystemException("Your appointment has been " + bookingStatus + " and cannot be cancelled.");
        }
        booking.setStatus(EBookingStatus.COMPLETED);
        booking.setUpdatedBy(currentUser.getUsername());
        bookingRepository.save(booking);
        response.setData(MessageConstants.COMPLETED_BOOKING_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getAllBookings(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (Objects.nonNull(doctor)) {
            Pageable pageable = commonServiceImpl.pagingSort(page, size, sorts);
            Page<BookingDetailsInfoProjection> bookingDetailsPage =
                    bookingRepository.getBookingDetailsByDoctorId(doctor.getDoctorId(), pageable);
            BookingResponse bookingResponse = BookingResponse.builder()
                    .totalItems(bookingDetailsPage.getTotalElements())
                    .totalPages(bookingDetailsPage.getTotalPages())
                    .currentPage(bookingDetailsPage.getNumber())
                    .bookings(bookingDetailsPage.getContent())
                    .build();
            response.setData(bookingResponse);
        }
        return response;
    }

    public Booking validateChangeBookingStatus(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        boolean checkAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().equals(ERole.ROLE_ADMIN));
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new ResourceNotFoundException("Booking id", bookingId.toString());
        }
        long doctorId = bookingRepository.getDoctorIdByBookingId(bookingId);
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (!checkAdmin) {
            if (doctor != null && !doctor.getDoctorId().equals(doctorId)) {
                throw new ForbiddenException(MessageConstants.FORBIDDEN_CHANGE_BOOKING_STATUS);
            }
        }
        return bookingOptional.get();
    }

    public boolean checkSpecializationExist(List<SpecializationDTO> specializations, SpecializationDTO specializationDTO) {
        boolean isExist = false;
        for (SpecializationDTO specialization : specializations) {
            if (specialization.equals(specializationDTO)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    public Set<WorkScheduleDTO> invalidWorkSchedules(List<Doctor> doctors, RegisterWorkScheduleRequest registerWorkSchedule) {
        Set<WorkScheduleDTO> invalidWorkSchedules = new HashSet<>();
        if (doctors.size() == 0) {
            List<WorkScheduleDTO> workSchedules = registerWorkSchedule.getWorkSchedules().stream()
                    .filter(newSchedule -> !isValidDuration(newSchedule.getStartTime(), newSchedule.getEndTime()))
                    .toList();
            invalidWorkSchedules.addAll(workSchedules);
        } else {
            doctors.forEach(doctor -> {
                List<WorkSchedule> existWorkSchedules =
                        workScheduleRepository.getWorkSchedulesByWorkingDay(doctor.getDoctorId(), registerWorkSchedule.getWorkingDay());
                List<WorkScheduleDTO> workScheduleDTOS = existWorkSchedules.stream().map(WorkScheduleMapper.WORK_SCHEDULE_MAPPER::mapToWorkScheduleDTO).toList();
                List<WorkScheduleDTO> filterWorkSchedules = registerWorkSchedule.getWorkSchedules().stream()
                        .filter(newSchedule -> !isValidDuration(newSchedule.getStartTime(), newSchedule.getEndTime()) ||
                                workScheduleDTOS.stream().anyMatch(existSchedule ->
                                        isTimeOverlap(newSchedule.getStartTime(), newSchedule.getEndTime(),
                                                existSchedule.getStartTime(), existSchedule.getEndTime())
                                )
                        ).toList();
                invalidWorkSchedules.addAll(filterWorkSchedules);
            });
        }
        return invalidWorkSchedules;
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean isValidDuration(LocalTime start, LocalTime end) {
        return Duration.between(start, end).toMinutes() == 30;
    }

    public String workSchedulesMessage(List<WorkScheduleDTO> invalidWorkSchedules, Date day) {
        String workSchedulesMessage;
        String workingDay = new SimpleDateFormat("dd-MM-yyyy").format(day);
        if (invalidWorkSchedules.size() == 0) {
            workSchedulesMessage = "Successfully registered work schedules for " + workingDay + ".";
        } else {
            StringBuilder invalidMessage = new StringBuilder();
            int size = invalidWorkSchedules.size();
            for (int i=0; i<size; i++) {
                invalidMessage.append(invalidWorkSchedules.get(i).getStartTime()).append("-")
                        .append(invalidWorkSchedules.get(i).getEndTime()).append((i != size-1) ? ", " : "");
            }
            workSchedulesMessage = "Successfully registered for work schedules for "+ workingDay
                    + ".However, the following time slots could not be registered because they are already taken: " + invalidMessage
                    + ". Note: Each work schedule time should only be 30 minutes.";
        }
        return workSchedulesMessage;
    }

    private EEducationLevel validateEducationLevel(String educationLevel) {
        String enumValue = "";
        if (educationLevel.equalsIgnoreCase("BACHELOR")) {
            enumValue = "BACHELOR";
        } else if (educationLevel.equalsIgnoreCase("MASTER")) {
            enumValue = "MASTER";
        } else if (educationLevel.equalsIgnoreCase("DOCTOR")) {
            enumValue = "DOCTOR";
        } else if (educationLevel.equalsIgnoreCase("PROFESSOR")) {
            enumValue = "PROFESSOR";
        } else if (educationLevel.equalsIgnoreCase("ASSOCIATE_PROFESSOR")) {
            enumValue = "ASSOCIATE_PROFESSOR";
        } else {
            throw new ResourceNotFoundException("Education level", educationLevel);
        }
        return EEducationLevel.valueOf(enumValue);
    }
}
