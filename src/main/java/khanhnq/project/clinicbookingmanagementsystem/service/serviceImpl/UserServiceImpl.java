package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.AddRoleDoctorRequest;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final FileRepository fileRepository;
    private final SkillRepository skillRepository;
    private final AuthService authService;

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
        return uploadFile(multipartFile, "avatar");
    }

    @Override
    public ResponseEntity<String> requestBecomeDoctor(AddRoleDoctorRequest addRoleDoctorRequest) {
        User currentUser = authService.getCurrentUser();
        if (!currentUser.getRoles().stream().filter(role -> role.getRoleName().equals(ERole.ROLE_DOCTOR)).findAny().isPresent()) {
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

    public ResponseEntity<String> uploadFile(MultipartFile multipartFile, String typeImage) {
        try {
            User currentUser = authService.getCurrentUser();
            File file = new File();
            if (!fileRepository.getFilesById(currentUser.getUserId()).stream().filter(f -> f.getFilePath().split("/")[1].equals(typeImage)).findAny().isPresent()) {
                file.setFilePath(currentUser.getUsername()+"/"+typeImage+"/"+StringUtils.cleanPath(multipartFile.getOriginalFilename()));
                file.setData(multipartFile.getBytes());
                file.setUser(currentUser);
                currentUser.getFiles().add(file);
            } else {
                file = fileRepository.getFileByType(typeImage, currentUser.getUserId());
                file.setFilePath(currentUser.getUsername()+"/"+typeImage+"/"+StringUtils.cleanPath(multipartFile.getOriginalFilename()));
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

}