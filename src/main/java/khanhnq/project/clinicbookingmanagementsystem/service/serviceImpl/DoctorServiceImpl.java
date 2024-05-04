package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EUserStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceNotFoundException;
import khanhnq.project.clinicbookingmanagementsystem.exception.UnauthorizedException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
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
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
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
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        commonServiceImpl.updateProfile(userProfileRequest, currentUser);
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_PROFILE_SUCCESS;
    }

    @Override
    public String uploadAvatar(MultipartFile multipartFile) {
        return commonServiceImpl.uploadFile(multipartFile, "avatar");
    }

    @Override
    public String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        List<SpecializationDTO> specializations = specializationRepository.findAll()
                .stream().map(SpecializationMapper.SPECIALIZATION_MAPPER::mapToSpecializationDTO).toList();
        if (!checkSpecializationExist(specializations, doctorInformationRequest.getSpecialization())) {
            throw new ResourceNotFoundException("Specialization",doctorInformationRequest.getSpecialization().getSpecializationName());
        }
        Set<Experience> experiences = doctorInformationRequest.getWorkExperiences()
                .stream().map(experienceDTO -> {
                    Experience experience = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperience(experienceDTO);
                    experience.setUser(currentUser);
                    experience.setCreatedBy(currentUser.getUsername());
                    return experience;
                }).collect(Collectors.toSet());
        currentUser.setSpecialization(specializationRepository.getSpecializationBySpecializationName(doctorInformationRequest.getSpecialization().getSpecializationName()));
        currentUser.setExperiences(experiences);
        currentUser.setCareerDescription(doctorInformationRequest.getCareerDescription());
        currentUser.setStatus(EUserStatus.PENDING);
        currentUser.setUpdatedBy(currentUser.getUsername());
        userRepository.save(currentUser);
        return MessageConstants.UPDATE_DOCTOR_INFORMATION_SUCCESS;
    }

    @Override
    public String uploadMedicalDegree(MultipartFile file) {
        return commonServiceImpl.uploadFile(file, "medical-degree");
    }

    @Override
    public String uploadSpecialtyDegree(MultipartFile file) {
        return commonServiceImpl.uploadFile(file, "specialty-degree");
    }

    @Override
    public String registerWorkSchedules(RegisterWorkScheduleRequest registerWorkSchedule) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR")) && currentUser.getStatus().equals(EUserStatus.PENDING)) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        if (Arrays.stream(DayOfWeek.values()).noneMatch(registerWorkSchedule.getDayOfWeek()::equals)) {
            throw new BusinessException(MessageConstants.INVALID_DAY_OF_WEEK);
        }
        List<WorkScheduleDTO> newWorkSchedules = registerWorkSchedule.getWorkSchedules();
        if (newWorkSchedules.size() != registerWorkSchedule.getNumberOfShiftsPerDay()) {
            throw new BusinessException(MessageConstants.INVALID_WORK_SCHEDULES);
        }
        DaysOfWeek oldDaysOfWeek = dayOfWeekRepository.getDayOfWeekByDay(currentUser.getUserId(), registerWorkSchedule.getDayOfWeek());
        if (Objects.nonNull(oldDaysOfWeek)) {
            dayOfWeekRepository.delete(oldDaysOfWeek);
        }
        List<User> doctors = userRepository.getDoctorsBySpecializationId(currentUser.getSpecialization().getSpecializationId())
                .stream().filter(user -> !user.equals(currentUser)).toList();
        List<WorkScheduleDTO> invalidWorkSchedules = getInvalidWorkSchedules(doctors, registerWorkSchedule);
        DaysOfWeek newDayOfWeek = DaysOfWeek.builder()
                .dayOfWeek(registerWorkSchedule.getDayOfWeek())
                .user(currentUser)
                .build();
        List<WorkSchedule> validWorkSchedules = newWorkSchedules.stream()
                .filter(workScheduleDTO -> !invalidWorkSchedules.contains(workScheduleDTO))
                .map(workScheduleDTO -> {
                    WorkSchedule workSchedule = WorkScheduleMapper.WORK_SCHEDULE_MAPPER.mapToWorkSchedule(workScheduleDTO);
                    workSchedule.setDaysOfWeek(newDayOfWeek);
                    workSchedule.setCreatedBy(currentUser.getUsername());
                    return workSchedule;
                }).toList();
        newDayOfWeek.setNumberOfShiftsPerDay(validWorkSchedules.size());
        newDayOfWeek.setWorkSchedules(validWorkSchedules);
        newDayOfWeek.setCreatedBy(currentUser.getUsername());
        List<DaysOfWeek> daysOfWeeks = currentUser.getDaysOfWeeks();
        daysOfWeeks.add(newDayOfWeek);
        currentUser.setDaysOfWeeks(daysOfWeeks);
        userRepository.save(currentUser);
        return workSchedulesMessage(invalidWorkSchedules, registerWorkSchedule.getDayOfWeek());
    }

    @Override
    public String confirmedBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR")) && currentUser.getStatus().equals(EUserStatus.PENDING)) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        bookingRepository.confirmedBooking(bookingId);
        return "Successful confirm booking.";
    }

    @Override
    public String cancelledBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR")) && currentUser.getStatus().equals(EUserStatus.PENDING)) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        bookingRepository.cancelledBooking(bookingId);
        return "Successful cancel booking.";
    }

    @Override
    public BookingResponse getAllBookings(int page, int size, String[] sorts) {
        User currentUser = authService.getCurrentUser();
        Page<Booking> bookingPage = bookingRepository.getAllBookings(currentUser.getUserId() , commonServiceImpl.pagingSort(page, size, sorts));
        List<BookingDTO> bookings = bookingPage.getContent()
                .stream()
                .map(booking -> {
                    BookingDTO bookingDTO = BookingMapper.BOOKING_MAPPER.mapToBookingDTO(booking);
                    bookingDTO.setUserAddress(commonServiceImpl.getAddress(booking));
                    bookingDTO.setStartTime(DateTimeFormatter.ofPattern("HH:mm").format(booking.getWorkSchedule().getStartTime()));
                    bookingDTO.setEndTime(DateTimeFormatter.ofPattern("HH:mm").format(booking.getWorkSchedule().getEndTime()));
                    return bookingDTO;
                }).toList();
        return BookingResponse.builder()
                .totalItems(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .currentPage(bookingPage.getNumber())
                .bookings(bookings)
                .build();
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
            DaysOfWeek daysOfWeek = dayOfWeekRepository.getDayOfWeekByDay(doctor.getUserId(), registerWorkSchedule.getDayOfWeek());
            List<WorkScheduleDTO> existWorkSchedules = workScheduleRepository.getWorkSchedulesByDayOfWeek(daysOfWeek)
                    .stream().map(WorkScheduleMapper.WORK_SCHEDULE_MAPPER::mapToWorkScheduleDTO).toList();
            List<WorkScheduleDTO> workSchedules = registerWorkSchedule.getWorkSchedules().stream()
                    .flatMap(newWorkSchedule -> existWorkSchedules.stream()
                            .filter(newWorkSchedule::equals)).toList();
            invalidWorkSchedules.addAll(workSchedules);
        });
        return invalidWorkSchedules;
    }

    public String workSchedulesMessage(List<WorkScheduleDTO> invalidWorkSchedules, DayOfWeek dayOfWeek) {
        String workSchedulesMessage;
        if (invalidWorkSchedules.size() == 0) {
            workSchedulesMessage = "Successfully registered work schedule for " + dayOfWeek + ".";
        } else {
            StringBuilder invalidMessage = new StringBuilder();
            int size = invalidWorkSchedules.size();
            for (int i=0; i<size; i++) {
                invalidMessage.append(invalidWorkSchedules.get(i).getStartTime()).append("-")
                        .append(invalidWorkSchedules.get(i).getEndTime()).append((i != size-1) ? ", " : "");
            }
            workSchedulesMessage = "Successfully registered for work schedules for "+ dayOfWeek
                    +". Except for work schedules "+invalidMessage+" because someone has already registered.";
        }
        return workSchedulesMessage;
    }

}
