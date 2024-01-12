package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.service.common.MethodsCommon;
import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
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
    public String confirmedBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new ResourceException("You don't have permission to do this.", HttpStatus.UNAUTHORIZED);
        }
        bookingRepository.confirmedBooking(bookingId);
        return "Successful booking confirmation.";
    }

    @Override
    public String cancelledBooking(Long bookingId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            throw new ResourceException("You don't have permission to do this.", HttpStatus.UNAUTHORIZED);
        }
        bookingRepository.cancelledBooking(bookingId);
        return "Successful booking cancelled. Please provide reasons why booking is cancelled for the patient.";
    }

    @Override
    public BookingResponse getAllBookings(int page, int size, String[] sorts) {
//        User currentUser = authService.getCurrentUser();
//        Page<Booking> bookingPage = bookingRepository.getAllBookings(currentUser.getUserId() , methodsCommon.pagingSort(page, size, sorts));
//        List<BookingDTO> bookings = bookingPage.getContent()
//                .stream()
//                .map(booking -> {
//                    BookingDTO bookingDTO = BookingMapper.BOOKING_MAPPER.mapToBookingDTO(booking);
//                    bookingDTO.setUserAddress(methodsCommon.getAddress(booking));
//                    bookingDTO.setStartTime(DateTimeFormatter.ofPattern("HH:mm").format(booking.getWorkSchedule().getStartTime()));
//                    bookingDTO.setEndTime(DateTimeFormatter.ofPattern("HH:mm").format(booking.getWorkSchedule().getEndTime()));
//                    return bookingDTO;
//                }).toList();
//        return BookingResponse.builder()
//                .totalItems(bookingPage.getTotalElements())
//                .totalPages(bookingPage.getTotalPages())
//                .currentPage(bookingPage.getNumber())
//                .bookings(bookings)
//                .build();
        return null;
    }

}
