package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EEducationLevel;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ForbiddenException;
import khanhnq.project.clinicbookingmanagementsystem.exception.UnauthorizedException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkScheduleMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.model.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public String updateProfile(UserProfileRequest userProfileRequest, MultipartFile avatar) {
        User currentUser = checkAccess();
        commonServiceImpl.updateProfile(userProfileRequest, currentUser, avatar);
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_PROFILE_SUCCESS;
    }

    @Override
    public String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest, MultipartFile specialtyDegree) {
        User currentUser = checkAccess();
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
        return MessageConstants.UPDATE_DOCTOR_INFORMATION_SUCCESS;
    }

    @Override
    public String registerWorkSchedules(RegisterWorkScheduleRequest registerWorkSchedule) {
        User currentUser = checkAccess();
        Doctor doctor = doctorRepository.findDoctorByUserId(currentUser.getUserId());
        if (Objects.isNull(doctor.getSpecialization())) {
            throw new BusinessException(MessageConstants.SPECIALIZATION_NOT_FOUND);
        }
        Date workingDay = registerWorkSchedule.getWorkingDay();
        LocalDate workingDayLocalDate = workingDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate nowLocalDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        if (workingDayLocalDate.isBefore(nowLocalDate) ||
                workingDayLocalDate.isBefore(startOfWeek) ||
                workingDayLocalDate.isAfter(endOfWeek)) {
            throw new BusinessException(MessageConstants.INVALID_WORKING_DAY);
        }
        DayOfWeek dayOfWeek = workingDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek();
        if (Arrays.stream(DayOfWeek.values()).noneMatch(dayOfWeek::equals)) {
            throw new BusinessException(MessageConstants.INVALID_DAY_OF_WEEK);
        }
        List<WorkScheduleDTO> newWorkSchedules = registerWorkSchedule.getWorkSchedules();
        if (newWorkSchedules.size() != registerWorkSchedule.getNumberOfShiftsPerDay()) {
            throw new BusinessException(MessageConstants.INVALID_WORK_SCHEDULES);
        }
        DaysOfWeek oldDaysOfWeek = dayOfWeekRepository.getDayOfWeekByDay(doctor.getDoctorId(), dayOfWeek);
        if (Objects.nonNull(oldDaysOfWeek)) {
            dayOfWeekRepository.delete(oldDaysOfWeek);
        }
        DaysOfWeek newDayOfWeek = Objects.nonNull(oldDaysOfWeek) ? oldDaysOfWeek : new DaysOfWeek();
        Long specializationId = doctor.getSpecialization().getSpecializationId();
        List<Doctor> doctors = doctorRepository.getDoctorsBySpecializationId(specializationId)
                .stream().filter(dt -> !dt.equals(doctor)).toList();
        List<WorkScheduleDTO> invalidWorkSchedules = invalidWorkSchedules(doctors, registerWorkSchedule);
        newDayOfWeek.setDayOfWeek(dayOfWeek);
        newDayOfWeek.setWorkingDay(workingDay);
        newDayOfWeek.setDoctor(doctor);
        List<WorkSchedule> validWorkSchedules = newWorkSchedules.stream()
                .filter(workScheduleDTO -> !invalidWorkSchedules.contains(workScheduleDTO))
                .map(workScheduleDTO -> {
                    WorkSchedule workSchedule = WorkScheduleMapper.WORK_SCHEDULE_MAPPER.mapToWorkSchedule(workScheduleDTO);
                    workSchedule.setDaysOfWeek(newDayOfWeek);
                    workSchedule.setCreatedBy(currentUser.getUsername());
                    workSchedule.setCreatedAt(LocalDateTime.now());
                    return workSchedule;
                }).toList();
        newDayOfWeek.setNumberOfShiftsPerDay(validWorkSchedules.size());
        newDayOfWeek.setWorkSchedules(validWorkSchedules);
        newDayOfWeek.setCreatedBy(currentUser.getUsername());
        newDayOfWeek.setCreatedAt(LocalDateTime.now());
        doctor.getDaysOfWeeks().add(newDayOfWeek);
        doctor.setDaysOfWeeks(doctor.getDaysOfWeeks());
        doctorRepository.save(doctor);
        return workSchedulesMessage(invalidWorkSchedules, workingDay);
    }

    @Override
    public String confirmedBooking(Long bookingId) {
        checkAccess();
        bookingRepository.confirmedBooking(bookingId);
        return MessageConstants.CONFIRM_BOOKING_SUCCESS;
    }

    @Override
    public String cancelledBooking(Long bookingId) {
        checkAccess();
        bookingRepository.cancelledBooking(bookingId);
        return MessageConstants.CANCELED_BOOKING_SUCCESS;
    }

    @Override
    public String completedBooking(Long bookingId) {
        checkAccess();
        bookingRepository.completedBooking(bookingId);
        return MessageConstants.COMPLETED_BOOKING_SUCCESS;
    }

    @Override
    public BookingResponse getAllBookings(int page, int size, String[] sorts) {
        User currentUser = authService.getCurrentUser();
        Pageable pageable = commonServiceImpl.pagingSort(page, size, sorts);
        Page<Booking> bookingPage = bookingRepository.getAllBookings(currentUser.getUserId() , pageable);
        return commonServiceImpl.getAllBookings(bookingPage);
    }

    public User checkAccess() {
        User currentUser = authService.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new ForbiddenException(MessageConstants.FORBIDDEN_ACCESS);
        }
        return currentUser;
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

    public List<WorkScheduleDTO> invalidWorkSchedules(List<Doctor> doctors, RegisterWorkScheduleRequest registerWorkSchedule) {
        List<WorkScheduleDTO> invalidWorkSchedules = new ArrayList<>();
        DayOfWeek dayOfWeek =
                registerWorkSchedule.getWorkingDay().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek();
        if (doctors.size() == 0) {
            List<WorkScheduleDTO> workSchedules = registerWorkSchedule.getWorkSchedules().stream()
                    .filter(newSchedule -> !isValidDuration(newSchedule.getStartTime(), newSchedule.getEndTime()))
                    .toList();
            invalidWorkSchedules.addAll(workSchedules);
        } else {
            doctors.forEach(doctor -> {
                DaysOfWeek daysOfWeek = dayOfWeekRepository.getDayOfWeekByDay(doctor.getDoctorId(), dayOfWeek);
                List<WorkScheduleDTO> existWorkSchedules = workScheduleRepository.getWorkSchedulesByDayOfWeek(daysOfWeek)
                        .stream().map(WorkScheduleMapper.WORK_SCHEDULE_MAPPER::mapToWorkScheduleDTO).toList();
                List<WorkScheduleDTO> workSchedules = registerWorkSchedule.getWorkSchedules().stream()
                        .filter(newSchedule -> !isValidDuration(newSchedule.getStartTime(), newSchedule.getEndTime()) ||
                                existWorkSchedules.stream().anyMatch(existSchedule ->
                                        isTimeOverlap(newSchedule.getStartTime(), newSchedule.getEndTime(),
                                                existSchedule.getStartTime(), existSchedule.getEndTime())
                                )
                        ).toList();
                invalidWorkSchedules.addAll(workSchedules);
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
