package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private FileService fileService;

    private UserRepository userRepository;

    private WardRepository wardRepository;

    private AddressRepository addressRepository;

    private FileRepository fileRepository;

    private SpecializationRepository specializationRepository;

    private SkillRepository skillRepository;

    private AuthService authService;

    public UserServiceImpl(FileService fileService,
                           UserRepository userRepository,
                           WardRepository wardRepository,
                           AddressRepository addressRepository,
                           FileRepository fileRepository,
                           SpecializationRepository specializationRepository,
                           SkillRepository skillRepository,
                           AuthService authService) {
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.wardRepository = wardRepository;
        this.addressRepository = addressRepository;
        this.fileRepository = fileRepository;
        this.specializationRepository = specializationRepository;
        this.skillRepository = skillRepository;
        this.authService = authService;
    }

    @Override
    public ResponseEntity<String> updateProfile(UserProfileRequest userProfileRequest) {
        User currentUser = authService.getCurrentUser();
        UserMapper.USER_MAPPER.mapToUser(currentUser, userProfileRequest);
        Address address = new Address();
        address.setSpecificAddress(userProfileRequest.getSpecificAddress());
        address.setWard(wardRepository.findById(userProfileRequest.getWardId()).orElse(null));
        currentUser.setAddress(address);
        userRepository.save(currentUser);
        return MessageResponse.getResponseMessage("Update profile successfully!", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> uploadAvatar(MultipartFile multipartFile) {
        try {
            fileService.save(multipartFile);
            User currentUser = authService.getCurrentUser();
            File file = new File();
            file.setFilePath("avatar/"+currentUser.getUsername()+"/"+multipartFile.getOriginalFilename());
            file.setUser(currentUser);
            fileRepository.save(file);
            userRepository.save(currentUser);
            return MessageResponse.getResponseMessage("Uploaded the file successfully: " + multipartFile.getOriginalFilename(), HttpStatus.OK);
        } catch (Exception e) {
            return MessageResponse.getResponseMessage("Could not upload the file: " + multipartFile.getOriginalFilename() + ". Error: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public ResponseEntity<String> addRoleDoctor(AddRoleDoctorRequest addRoleDoctorRequest) {
//        try {
//            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            User currentUser = authService.getCurrentUser();
            currentUser.setUniversityName(addRoleDoctorRequest.getUniversityName());
            Set<Experience> experiences = addRoleDoctorRequest.getExperiences().stream()
                    .map(experienceRequest -> Experience.builder()
                            .clinicName(experienceRequest.getClinicName())
                            .position(experienceRequest.getPosition())
                            .startWork(experienceRequest.getStartWork())
                            .endWork(experienceRequest.getEndWork())
                            .jobDescription(experienceRequest.getJobDescription())
                            .specialization(specializationRepository.findById(experienceRequest.getSpecializationId()).orElse(null))
                            .skills(experienceRequest.getSkillIds().stream()
                                    .map(ids -> skillRepository.findById(ids).orElse(null))
                                    .collect(Collectors.toSet()))
                            .user(currentUser)
                            .build())
                    .collect(Collectors.toSet());

            currentUser.setExperiences(experiences);
            fileService.save(addRoleDoctorRequest.getMedicalLicense());
            fileService.save(addRoleDoctorRequest.getMedicalDegree());
            currentUser.getFiles().add(File.builder()
                    .filePath("medical-degree/"+currentUser.getUsername()+"/"+addRoleDoctorRequest.getMedicalDegree().getOriginalFilename())
                    .build());
            currentUser.getFiles().add(File.builder()
                    .filePath("medical-license/"+currentUser.getUsername()+"/"+addRoleDoctorRequest.getMedicalLicense().getOriginalFilename())
                    .build());
            for (File file : currentUser.getFiles()) {
                file.setUser(currentUser);
                fileRepository.save(file);
            }
            userRepository.save(currentUser);
            return MessageResponse.getResponseMessage("Request to become doctor successfully. Waiting for accept...", HttpStatus.OK);
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
    }
}