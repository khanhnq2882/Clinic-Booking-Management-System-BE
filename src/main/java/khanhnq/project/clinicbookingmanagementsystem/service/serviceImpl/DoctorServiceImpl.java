package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.AddressResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final AuthService authService;
    private final WardRepository wardRepository;
    private final SkillRepository skillRepository;
    private final SpecializationRepository specializationRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final BookingRepository bookingRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public String addDoctorInformation(DoctorInformationRequest doctorInformationRequest) {
        User currentUser = authService.getCurrentUser();
        Set<WorkSchedule> workSchedules = doctorInformationRequest.getWorkSchedules().stream()
                .map(workScheduleDTO -> WorkSchedule.builder()
                        .startTime(workScheduleDTO.getStartTime())
                        .endTime(workScheduleDTO.getEndTime())
                        .user(currentUser)
                        .build())
                .collect(Collectors.toSet());
        if (currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_DOCTOR"))) {
            UserMapper.USER_MAPPER.mapToDoctor(currentUser, doctorInformationRequest);
            currentUser.setAddress(Address.builder()
                    .specificAddress(doctorInformationRequest.getSpecificAddress())
                    .ward(wardRepository.findById(doctorInformationRequest.getWardId()).orElse(null))
                    .build());
            Set<WorkSchedule> sortWorkSchedules = new LinkedHashSet<>(addWorkSchedules(workSchedules, currentUser.getSpecialization().getSpecializationId()));
            currentUser.setWorkSchedules(sortWorkSchedules);
            Set<Skill> skills = doctorInformationRequest.getSkillIds()
                    .stream()
                    .map(id -> skillRepository.findById(id).orElse(null))
                    .collect(Collectors.toSet());
            currentUser.setSkills(skills);
            userRepository.save(currentUser);
        }
        return "Update information successfully.";
    }

    @Override
    public List<BookingDTO> getAllUserBookings() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        return groupBookingsByDoctor()
                .stream()
                .map(booking -> {
                    BookingDTO bookingDTO = BookingMapper.BOOKING_MAPPER.mapToBookingDTO(booking);
                    bookingDTO.setUserAddress(getAddress(booking));
                    bookingDTO.setStartTime(dtf.format(booking.getWorkSchedule().getStartTime()));
                    bookingDTO.setEndTime(dtf.format(booking.getWorkSchedule().getEndTime()));
                    return bookingDTO;
                }).toList();
    }

    @Override
    public String changeBookingStatus(Long bookingId, String status) {
        bookingRepository.changeBookingStatus(bookingId, status);
        return null;
    }


    public AddressResponse getAddress(Booking booking) {
        Address address = addressRepository.findById(booking.getAddress().getAddressId()).orElse(null);
        new AddressResponse();
        return AddressResponse.builder()
                .addressId(Objects.requireNonNull(address).getAddressId())
                .specificAddress(address.getSpecificAddress())
                .wardName(address.getWard().getWardName())
                .districtName(address.getWard().getDistrict().getDistrictName())
                .cityName(address.getWard().getDistrict().getCity().getCityName())
                .build();
    }

    public List<Booking> groupBookingsByDoctor () {
        User currentUser = authService.getCurrentUser();
        Map<Long, List<Booking>> map = new HashMap<>();
        for (WorkSchedule workSchedule : groupWorkScheduleByDoctor().get(currentUser.getUserId())) {
            List<Booking> bookings = bookingRepository.getAllUserBookings(workSchedule.getWorkScheduleId());
            if (bookings.size() != 0) {
                if (Objects.isNull(map.get(currentUser.getUserId()))) {
                    map.put(currentUser.getUserId(), bookings);
                } else {
                    map.get(currentUser.getUserId()).addAll(bookings);
                }
            }
        }
        return map.get(currentUser.getUserId());
    }

    public Map<Long, List<User>> groupDoctorsBySpecialization() {
        Map<Long, List<User>> map = new HashMap<>();
        for (Specialization specialization : specializationRepository.findAll()) {
            if (!map.containsKey(specialization.getSpecializationId())) {
                List<User> users = userRepository.getDoctorsBySpecializationId(specialization.getSpecializationId());
                map.put(specialization.getSpecializationId(), users);
            }
        }
        return map;
    }

    public Map<Long, Set<WorkSchedule>> groupWorkScheduleByDoctor() {
        Map<Long, Set<WorkSchedule>> map = new HashMap<>();
        for (User user : userRepository.getDoctors()) {
            if (!map.containsKey(user.getUserId())) {
                Set<WorkSchedule> workSchedules = new HashSet<>(workScheduleRepository.getWorkSchedulesByUserId(user.getUserId()));
                map.put(user.getUserId(), workSchedules);
            }
        }
        return map;
    }

    public Set<WorkSchedule> addWorkSchedules(Set<WorkSchedule> workSchedulesRequest, Long specializationId) {
        // giam bot vong for
        Set<WorkSchedule> similarWorkSchedule = new HashSet<>();
        Map<Long, List<User>> specializationMap = groupDoctorsBySpecialization();
        Map<Long, Set<WorkSchedule>> userMap = groupWorkScheduleByDoctor();
        for (User doctor : specializationMap.get(specializationId)) {
            for (WorkSchedule workSchedule : userMap.get(doctor.getUserId())) {
                for (WorkSchedule workScheduleRequest : workSchedulesRequest) {
                    if (workScheduleRequest.getStartTime().equals(workSchedule.getStartTime())
                            && workScheduleRequest.getEndTime().equals(workSchedule.getEndTime())) {
                        similarWorkSchedule.add(workScheduleRequest);
                    }
                }
            }
        }
        workSchedulesRequest.removeAll(similarWorkSchedule);
        return workSchedulesRequest;
    }

}
