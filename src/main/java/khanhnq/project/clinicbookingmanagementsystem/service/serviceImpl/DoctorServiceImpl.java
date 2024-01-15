package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.request.RegisterWorkScheduleRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.common.MethodsCommon;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceException;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final MethodsCommon methodsCommon;

    @Override
    public String updateDoctorInformation(DoctorInformationRequest doctorInformationRequest) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new ResourceException("You don't have permission to do this.", HttpStatus.UNAUTHORIZED);
        }
        methodsCommon.updateProfile(doctorInformationRequest.getProfileRequest(), currentUser);
        Set<Experience> experiences = doctorInformationRequest.getWorkExperiences()
                .stream()
                .map(experienceDTO -> {
                    Experience experience = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperience(experienceDTO);
                    experience.setUser(currentUser);
                    return experience;
                }).collect(Collectors.toSet());
        currentUser.setProfessionalDescription(doctorInformationRequest.getProfessionalDescription());
        currentUser.setExperiences(experiences);
        userRepository.save(currentUser);
        return "Update doctor information successfully.";
    }

    @Override
    public String registerWorkSchedules(RegisterWorkScheduleRequest registerWorkSchedule) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new ResourceException("You don't have permission to do this.", HttpStatus.UNAUTHORIZED);
        }
        List<DaysOfWeek> daysOfWeeks = new ArrayList<>();
        StringBuilder invalidMessage = new StringBuilder();
        if (Arrays.stream(DayOfWeek.values()).noneMatch(dayOfWeek -> registerWorkSchedule.getDayOfWeek().equalsIgnoreCase(dayOfWeek.name()))) {
            throw new ResourceException("Day of week invalid.", HttpStatus.BAD_REQUEST);
        }
        if (registerWorkSchedule.getWorkSchedules().size() != registerWorkSchedule.getNumberOfShiftsPerDay()) {
            throw new ResourceException("You have not registered for "+registerWorkSchedule.getNumberOfShiftsPerDay()+" shifts on "
                    +registerWorkSchedule.getDayOfWeek().toUpperCase(), HttpStatus.BAD_REQUEST);
        }
        DayOfWeek day = DayOfWeek.valueOf(registerWorkSchedule.getDayOfWeek().toUpperCase());
        DaysOfWeek oldDaysOfWeek = dayOfWeekRepository.getDayOfWeekByDay(currentUser.getUserId(), day);
        if (Objects.nonNull(oldDaysOfWeek)) {
            dayOfWeekRepository.delete(oldDaysOfWeek);
        }
        DaysOfWeek newDaysOfWeek = DaysOfWeek.builder()
                .dayOfWeek(day)
                .numberOfShiftsPerDay(registerWorkSchedule.getNumberOfShiftsPerDay())
                .user(currentUser)
                .build();
        List<WorkSchedule> existWorkSchedules = workScheduleRepository.getWorkSchedulesByDay(currentUser.getSpecialization().getSpecializationId(), day);
        List<WorkScheduleDTO> invalidWorkSchedules = new ArrayList<>();
        for (WorkSchedule workSchedule : existWorkSchedules) {
            for (WorkScheduleDTO workScheduleDTO : registerWorkSchedule.getWorkSchedules()) {
                LocalTime newStartTime = workScheduleDTO.getStartTime();
                LocalTime newEndTime = workScheduleDTO.getEndTime();
                if((newStartTime.isAfter(workSchedule.getStartTime()) && newEndTime.isBefore(workSchedule.getEndTime()))
                        || (newStartTime.equals(workSchedule.getStartTime()) && newEndTime.equals(workSchedule.getEndTime()))) {
                    invalidWorkSchedules.add(workScheduleDTO);
                }
            }
        }
        registerWorkSchedule.getWorkSchedules().removeAll(invalidWorkSchedules);
        List<WorkSchedule> newWorkSchedules = registerWorkSchedule.getWorkSchedules()
                .stream()
                .map(workScheduleDTO -> WorkSchedule
                        .builder()
                        .startTime(workScheduleDTO.getStartTime())
                        .endTime(workScheduleDTO.getEndTime())
                        .daysOfWeek(newDaysOfWeek)
                        .build())
                .toList();
        newDaysOfWeek.setWorkSchedules(newWorkSchedules);
        daysOfWeeks.add(newDaysOfWeek);
        currentUser.setDaysOfWeeks(daysOfWeeks);
        userRepository.save(currentUser);
        String message = "";
        if (invalidWorkSchedules.size() == 0) {
            message += "Successfully registered for work schedules for "+day;
        } else {
            for (WorkScheduleDTO invalidWorkSchedule : invalidWorkSchedules) {
                invalidMessage.append(invalidWorkSchedule.getStartTime()).append(" - ").append(invalidWorkSchedule.getEndTime()).append(", ");
            }
            message += "Successfully registered for work schedules for "+day+". Except for work schedules "+invalidMessage+" because someone has already registered.";
        }
        return message;
    }

    @Override
    public String confirmedBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new ResourceException("You don't have permission to do this.", HttpStatus.UNAUTHORIZED);
        }
        bookingRepository.confirmedBooking(bookingId);
        return "Successful confirm booking.";
    }

    @Override
    public String cancelledBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new ResourceException("You don't have permission to do this.", HttpStatus.UNAUTHORIZED);
        }
        bookingRepository.cancelledBooking(bookingId);
        return "Successful cancel booking.";
    }

    @Override
    public BookingResponse getAllBookings(int page, int size, String[] sorts) {
        User currentUser = authService.getCurrentUser();
        Page<Booking> bookingPage = bookingRepository.getAllBookings(currentUser.getUserId() , methodsCommon.pagingSort(page, size, sorts));
        List<BookingDTO> bookings = bookingPage.getContent()
                .stream()
                .map(booking -> {
                    BookingDTO bookingDTO = BookingMapper.BOOKING_MAPPER.mapToBookingDTO(booking);
                    bookingDTO.setUserAddress(methodsCommon.getAddress(booking));
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
