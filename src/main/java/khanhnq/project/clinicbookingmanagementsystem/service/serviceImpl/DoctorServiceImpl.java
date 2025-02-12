package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ForbiddenException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.exception.UnauthorizedException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.SpecializationMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkScheduleMapper;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
    private final SpecializationRepository specializationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final CommonServiceImpl commonServiceImpl;

    @Override
    public String updateProfile(UserProfileRequest userProfileRequest) {
        User currentUser = checkAccess();
//        commonServiceImpl.updateProfile(userProfileRequest, currentUser);
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_PROFILE_SUCCESS;
    }

    @Override
    public String uploadAvatar(MultipartFile multipartFile) {
//        return commonServiceImpl.uploadFile(multipartFile, "avatar");
        return "";
    }

    @Override
    public String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest) {
        User currentUser = checkAccess();
        List<SpecializationDTO> specializations = specializationRepository.findAll()
                .stream().map(SpecializationMapper.SPECIALIZATION_MAPPER::mapToSpecializationDTO).toList();
        String specializationName = doctorInformationRequest.getSpecialization().getSpecializationName();
        if (!checkSpecializationExist(specializations, doctorInformationRequest.getSpecialization())) {
            throw new ResourceNotFoundException("Specialization", specializationName);
        }
        Set<Experience> experiences = doctorInformationRequest.getWorkExperiences()
                .stream().map(experienceDTO -> {
                    Experience experience = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperience(experienceDTO);
//                    experience.setUser(currentUser);
                    experience.setCreatedBy(currentUser.getUsername());
                    return experience;
                }).collect(Collectors.toSet());
        Specialization specialization  = specializationRepository.getSpecializationBySpecializationName(specializationName);
//        currentUser.setSpecialization(specialization);
//        currentUser.setExperiences(experiences);
//        currentUser.setCareerDescription(doctorInformationRequest.getCareerDescription());
        currentUser.setStatus(EUserStatus.PENDING);
        currentUser.setUpdatedBy(currentUser.getUsername());
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_DOCTOR_INFORMATION_SUCCESS;
    }

    @Override
    public String uploadMedicalDegree(MultipartFile file) {
//        return commonServiceImpl.uploadFile(file, "medical-degree");
        return "";
    }

    @Override
    public String uploadSpecialtyDegree(MultipartFile file) {
//        return commonServiceImpl.uploadFile(file, "specialty-degree");
        return "";
    }

    @Override
    public String registerWorkSchedules(RegisterWorkScheduleRequest registerWorkSchedule) {
        User currentUser = checkAccess();
//        if (Objects.isNull(currentUser.getSpecialization())) {
//            throw new BusinessException(MessageConstants.SPECIALIZATION_NOT_FOUND);
//        }
        Date workingDay = registerWorkSchedule.getWorkingDay();
        LocalDate ld1 = workingDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ld2 = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ld1.isBefore(ld2)) {
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
        DaysOfWeek oldDaysOfWeek = dayOfWeekRepository.getDayOfWeekByDay(currentUser.getUserId(), dayOfWeek);
        if (Objects.nonNull(oldDaysOfWeek)) {
            dayOfWeekRepository.delete(oldDaysOfWeek);
        }
//        List<User> doctors = userRepository.getDoctorsBySpecializationId(currentUser.getSpecialization().getSpecializationId())
//                .stream().filter(user -> !user.equals(currentUser)).toList();
//        List<WorkScheduleDTO> invalidWorkSchedules = getInvalidWorkSchedules(doctors, registerWorkSchedule);
//        DaysOfWeek newDayOfWeek = new DaysOfWeek();
//        newDayOfWeek.setDayOfWeek(dayOfWeek);
//        newDayOfWeek.setWorkingDay(workingDay);
//        newDayOfWeek.setUser(currentUser);
//        List<WorkSchedule> validWorkSchedules = newWorkSchedules.stream()
//                .filter(workScheduleDTO -> !invalidWorkSchedules.contains(workScheduleDTO))
//                .map(workScheduleDTO -> {
//                    WorkSchedule workSchedule = WorkScheduleMapper.WORK_SCHEDULE_MAPPER.mapToWorkSchedule(workScheduleDTO);
//                    workSchedule.setDaysOfWeek(newDayOfWeek);
//                    workSchedule.setCreatedBy(currentUser.getUsername());
//                    return workSchedule;
//                }).toList();
//        newDayOfWeek.setNumberOfShiftsPerDay(validWorkSchedules.size());
//        newDayOfWeek.setWorkSchedules(validWorkSchedules);
//        newDayOfWeek.setCreatedBy(currentUser.getUsername());
//        currentUser.getDaysOfWeeks().add(newDayOfWeek);
//        currentUser.setDaysOfWeeks(currentUser.getDaysOfWeeks());
//        userRepository.save(currentUser);
//        return workSchedulesMessage(invalidWorkSchedules, workingDay);
        return "";
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
        Page<Booking> bookingPage = bookingRepository.getAllBookings(currentUser.getUserId() , commonServiceImpl.pagingSort(page, size, sorts));
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

    public List<WorkScheduleDTO> getInvalidWorkSchedules (List<User> doctors, RegisterWorkScheduleRequest registerWorkSchedule) {
        List<WorkScheduleDTO> invalidWorkSchedules = new ArrayList<>();
        doctors.forEach(doctor -> {
            DaysOfWeek daysOfWeek = dayOfWeekRepository.getDayOfWeekByDay(doctor.getUserId(),
                    registerWorkSchedule.getWorkingDay().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek());
            List<WorkScheduleDTO> existWorkSchedules = workScheduleRepository.getWorkSchedulesByDayOfWeek(daysOfWeek)
                    .stream().map(WorkScheduleMapper.WORK_SCHEDULE_MAPPER::mapToWorkScheduleDTO).toList();
            List<WorkScheduleDTO> workSchedules = registerWorkSchedule.getWorkSchedules().stream()
                    .flatMap(newWorkSchedule -> existWorkSchedules.stream()
                            .filter(newWorkSchedule::equals)).toList();
            invalidWorkSchedules.addAll(workSchedules);
        });
        return invalidWorkSchedules;
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
                    +". Except for work schedules "+invalidMessage+" because someone has already registered.";
        }
        return workSchedulesMessage;
    }

}
