package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.UnauthorizedException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
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
//        commonServiceImpl.updateProfile(doctorInformationRequest.getProfileRequest(), currentUser);
//        Set<Experience> experiences = doctorInformationRequest.getWorkExperiences()
//                .stream()
//                .map(experienceDTO -> {
//                    Experience experience = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperience(experienceDTO);
//                    experience.setUser(currentUser);
//                    return experience;
//                }).collect(Collectors.toSet());
//        currentUser.setProfessionalDescription(doctorInformationRequest.getProfessionalDescription());
//        currentUser.setExperiences(experiences);
//        userRepository.save(currentUser);
        return "Update doctor information successfully.";
    }

    @Override
    public String registerWorkSchedules(RegisterWorkScheduleRequest registerWorkSchedule) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        List<DaysOfWeek> daysOfWeeks = new ArrayList<>();
        StringBuilder invalidMessage = new StringBuilder();
        DayOfWeek dayOfWeek = registerWorkSchedule.getDayOfWeek().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek();
        if (Arrays.stream(DayOfWeek.values()).noneMatch(dayOfWeek::equals)) {
            throw new BusinessException(MessageConstants.INVALID_DAY_OF_WEEK);
        }
        if (registerWorkSchedule.getWorkSchedules().size() != registerWorkSchedule.getNumberOfShiftsPerDay()) {
            throw new BusinessException("You have not registered for "+registerWorkSchedule.getNumberOfShiftsPerDay()+" shifts on " +dayOfWeek);
        }
        DaysOfWeek oldDayOfWeek = dayOfWeekRepository.getDayOfWeekByDay(currentUser.getUserId(), dayOfWeek);
        if (Objects.nonNull(oldDayOfWeek)) {
            dayOfWeekRepository.delete(oldDayOfWeek);
        }
        DaysOfWeek newDayOfWeek = DaysOfWeek.builder()
                .dayOfWeek(dayOfWeek)
                .numberOfShiftsPerDay(registerWorkSchedule.getNumberOfShiftsPerDay())
                .user(currentUser)
                .build();
        List<WorkSchedule> existWorkSchedules = workScheduleRepository.getWorkSchedulesByDay(currentUser.getSpecialization().getSpecializationId(), dayOfWeek);
        List<WorkScheduleDTO> invalidWorkSchedules = existWorkSchedules.stream()
                .flatMap(workSchedule -> registerWorkSchedule.getWorkSchedules().stream()
                        .filter(workScheduleDTO ->
                                (workScheduleDTO.getStartTime().isAfter(workSchedule.getStartTime()) &&
                                        workScheduleDTO.getEndTime().isBefore(workSchedule.getEndTime())) ||
                                        (workScheduleDTO.getStartTime().equals(workSchedule.getStartTime()) &&
                                                workScheduleDTO.getEndTime().equals(workSchedule.getEndTime()))
                        )
                ).toList();
        registerWorkSchedule.getWorkSchedules().removeAll(invalidWorkSchedules);
        List<WorkSchedule> newWorkSchedules = registerWorkSchedule.getWorkSchedules()
                .stream()
                .map(workScheduleDTO -> WorkSchedule
                        .builder()
                        .startTime(workScheduleDTO.getStartTime())
                        .endTime(workScheduleDTO.getEndTime())
                        .daysOfWeek(newDayOfWeek)
                        .build())
                .toList();
        newDayOfWeek.setWorkSchedules(newWorkSchedules);
        daysOfWeeks.add(newDayOfWeek);
        currentUser.setDaysOfWeeks(daysOfWeeks);
        userRepository.save(currentUser);
        String responseMessage = "";
        if (invalidWorkSchedules.size() == 0) {
            responseMessage = "Successfully registered for work schedules for "+dayOfWeek;
        } else {
            for (WorkScheduleDTO invalidWorkSchedule : invalidWorkSchedules) {
                invalidMessage.append(invalidWorkSchedule.getStartTime()).append(" - ").append(invalidWorkSchedule.getEndTime()).append(", ");
            }
            responseMessage = "Successfully registered for work schedules for "+dayOfWeek+". Except for work schedules "+invalidMessage+" because someone has already registered.";
        }
        return responseMessage;
    }

    @Override
    public String confirmedBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        bookingRepository.confirmedBooking(bookingId);
        return "Successful confirm booking.";
    }

    @Override
    public String cancelledBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
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

}
