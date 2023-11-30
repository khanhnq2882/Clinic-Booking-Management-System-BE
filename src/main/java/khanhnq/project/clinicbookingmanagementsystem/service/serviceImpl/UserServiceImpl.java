package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.DoctorDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.SkillDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.BookingMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.BookingAppointmentRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final FileRepository fileRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final SkillRepository skillRepository;
    private final BookingRepository bookingRepository;
    private final AuthService authService;

    @Override
    public ResponseEntity<String> updateProfile(UserProfileRequest userProfileRequest) {
        User currentUser = authService.getCurrentUser();
        UserMapper.USER_MAPPER.mapToUser(currentUser, userProfileRequest);
        currentUser.setAddress(Address.builder()
                .specificAddress(userProfileRequest.getSpecificAddress())
                .ward(wardRepository.findById(userProfileRequest.getWardId()).orElse(null))
                .build());
        userRepository.save(currentUser);
        return MessageResponse.getResponseMessage("Update profile successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> uploadAvatar(MultipartFile multipartFile) {
        return uploadFile(multipartFile, "avatar");
    }

    @Override
    public ResponseEntity<String> requestBecomeDoctor(AddRoleDoctorRequest addRoleDoctorRequest) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().equals(ERole.ROLE_DOCTOR))) {
            currentUser.setUniversityName(addRoleDoctorRequest.getUniversityName());
            Set<Experience> experiences = addRoleDoctorRequest.getExperiences().stream()
                    .map(experienceRequest -> {
                        Experience experience = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperience(experienceRequest);
                        experience.setSkills(experienceRequest.getSkillIds()
                                .stream()
                                .map(id -> skillRepository.findById(id).orElse(null))
                                .collect(Collectors.toSet()));
                        experience.setUser(currentUser);
                        return experience;
                    }).collect(Collectors.toSet());
            currentUser.setExperiences(experiences);
            userRepository.save(currentUser);
            return MessageResponse.getResponseMessage("Request to become doctor successfully. Waiting for accept...", HttpStatus.OK);
        } else {
            return MessageResponse.getResponseMessage("There is no need to submit a request because you are already a doctor in the system.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<String> uploadMedicalLicense(MultipartFile multipartFile) {
        return uploadFile(multipartFile, "medical-license");
    }

    @Override
    public ResponseEntity<String> uploadMedicalDegree(MultipartFile multipartFile) {
        return uploadFile(multipartFile, "medical-degree");
    }

    @Override
    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(skill -> SkillDTO.builder()
                        .skillId(skill.getSkillId())
                        .skillName(skill.getSkillName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorDTO> getDoctorsBySpecialization(Long specializationId) {
        return userRepository.getDoctorsBySpecializationId(specializationId)
                .stream()
                .map(user -> DoctorDTO.builder()
                        .userId(user.getUserId())
                        .userCode(user.getUserCode())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkScheduleDTO> getWorkSchedulesByDoctor(Long userId) {
        return workScheduleRepository.getWorkSchedulesByUserId(userId)
                .stream()
                .map(workSchedule -> WorkScheduleDTO.builder()
                        .workScheduleId(workSchedule.getWorkScheduleId())
                        .startTime(workSchedule.getStartTime())
                        .endTime(workSchedule.getEndTime())
                        .build())
                .toList();
    }

    @Override
    public String bookingAppointment(BookingAppointmentRequest bookingAppointmentRequest) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String appointmentDate = dateFormat.format(bookingAppointmentRequest.getAppointmentDate());
        User currentUser = authService.getCurrentUser();
        Booking bookingAppointment = BookingMapper.BOOKING_MAPPER.mapToBooking(bookingAppointmentRequest);
        bookingAppointment.setAddress(Address.builder()
                .specificAddress(bookingAppointmentRequest.getSpecificAddress())
                .ward(wardRepository.findById(bookingAppointmentRequest.getWardId()).orElse(null))
                .build());
        WorkSchedule workSchedule = workScheduleRepository.findById(bookingAppointmentRequest.getWorkScheduleId()).orElse(null);
        for (Booking booking : bookingRepository.findAll()) {
            if (bookingAppointmentRequest.getWorkScheduleId().equals(booking.getWorkSchedule().getWorkScheduleId())
            && appointmentDate.equals(booking.getAppointmentDate().toString())) {
                throw new ResourceException("You cannot schedule an appointment at time "+ Objects.requireNonNull(workSchedule).getStartTime() +" - "+ workSchedule.getEndTime()
                        +" on day "+appointmentDate, HttpStatus.BAD_REQUEST);
            }
        }
        bookingAppointment.setBookingCode(bookingCode());
        bookingAppointment.setWorkSchedule(workSchedule);
        bookingAppointment.setStatus(EBookingStatus.PENDING);
        bookingAppointment.setUser(currentUser);
        bookingRepository.save(bookingAppointment);
        return "Booking appointment successfully.";
    }

    public ResponseEntity<String> uploadFile(MultipartFile multipartFile, String typeImage) {
        try {
            User currentUser = authService.getCurrentUser();
            File file = new File();
            if (fileRepository.getFilesById(currentUser.getUserId()).stream().noneMatch(f -> f.getFilePath().split("/")[1].equals(typeImage))) {
                file.setFilePath(currentUser.getUsername()+"/"+typeImage+"/"+StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename())));
                file.setData(multipartFile.getBytes());
                file.setUser(currentUser);
                currentUser.getFiles().add(file);
            } else {
                file = fileRepository.getFileByType(typeImage, currentUser.getUserId());
                file.setFilePath(currentUser.getUsername()+"/"+typeImage+"/"+StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename())));
                file.setData(multipartFile.getBytes());
                file.setUser(currentUser);
            }
            fileRepository.save(file);
            userRepository.save(currentUser);
            return MessageResponse.getResponseMessage("Uploaded the file" +typeImage+ " successfully: " + multipartFile.getOriginalFilename(), HttpStatus.OK);
        } catch (Exception e) {
            return MessageResponse.getResponseMessage("Could not upload the file"+ typeImage+ " : " + multipartFile.getOriginalFilename() + ". Error: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public String bookingCode() {
        String code;
        if (bookingRepository.findAll().size() == 0) {
            code = "BC1";
        } else {
            Long maxServiceCode = Collections.max(bookingRepository.findAll()
                    .stream()
                    .map(booking -> Long.parseLong(booking.getBookingCode().substring(2)))
                    .toList());
            code = "BC" + (maxServiceCode+1);
        }
        return code;
    }

}