package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.FileRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import khanhnq.project.clinicbookingmanagementsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final FileService fileService;

    private final UserRepository userRepository;

    private final WardRepository wardRepository;

    private final AddressRepository addressRepository;

    private final FileRepository fileRepository;

    private final SpecializationRepository specializationRepository;

    private final SkillRepository skillRepository;

    private final AuthService authService;

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
    public ResponseEntity<String> requestBecomeDoctor(AddRoleDoctorRequest addRoleDoctorRequest) {
        User currentUser = authService.getCurrentUser();
        currentUser.setUniversityName(addRoleDoctorRequest.getUniversityName());
        Set<Experience> experiences = addRoleDoctorRequest.getExperiences().stream()
                .map(experienceRequest -> {
                    Experience experience = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperience(experienceRequest);
                    experience.setSkills(experienceRequest.getSkillIds().stream()
                            .map(id -> skillRepository.findById(id).orElse(null))
                            .collect(Collectors.toSet()));
                    experience.setUser(currentUser);
                    return experience;
                }).collect(Collectors.toSet());
        currentUser.setExperiences(experiences);
        userRepository.save(currentUser);
        return MessageResponse.getResponseMessage("Request to become doctor successfully. Waiting for accept...", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> uploadLicenseDegree(FileRequest fileRequest) {
        User currentUser = authService.getCurrentUser();
        fileService.save(fileRequest.getMedicalDegree());
        fileService.save(fileRequest.getMedicalLicense());
        currentUser.getFiles().add(File.builder()
                .filePath("medical-degree/"+currentUser.getUsername()+"/"+fileRequest.getMedicalDegree().getOriginalFilename())
                .build());
        currentUser.getFiles().add(File.builder()
                .filePath("medical-license/"+currentUser.getUsername()+"/"+fileRequest.getMedicalLicense().getOriginalFilename())
                .build());
        for (File file : currentUser.getFiles()) {
            file.setUser(currentUser);
            fileRepository.save(file);
        }
        userRepository.save(currentUser);
        return MessageResponse.getResponseMessage("Upload file successfully!", HttpStatus.OK);
    }

}