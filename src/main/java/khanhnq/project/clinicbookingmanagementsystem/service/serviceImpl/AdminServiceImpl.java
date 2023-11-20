package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.Address;
import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.response.FileResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.MessageResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.RequestDoctorResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.UserResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final FileService fileService;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final ExperienceRepository experienceRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            AuthService authService,
                            RoleRepository roleRepository,
                            AddressRepository addressRepository,
                            ExperienceRepository experienceRepository,
                            FileService fileService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.experienceRepository = experienceRepository;
        this.fileService = fileService;
    }

    @Override
    public ResponseEntity<String> updateUserRoles(Long userId) {
        User currentUser = authService.getCurrentUser();
        User user = userRepository.findById(userId).orElse(null);
        if (currentUser.getRoles().stream().filter(role -> role.getRoleName().equals(ERole.ROLE_ADMIN)).findAny().isPresent()) {
            Role role = roleRepository.findRoleByRoleName(ERole.ROLE_DOCTOR);
            if (role == null) {
                roleRepository.save(Role.builder().roleName(ERole.ROLE_DOCTOR).build());
            }
            user.getRoles().add(role);
            userRepository.save(user);
            return MessageResponse.getResponseMessage("Update successfully .User "+user.getFirstName()+" "+user.getLastName()+" is became a doctor in the system.", HttpStatus.OK);
        }
        return MessageResponse.getResponseMessage("You do not have permission to update user roles.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> userList = userRepository.getAllUsers().stream().map(user -> {
            UserResponse userResponse = UserMapper.USER_MAPPER.mapToUserResponse(user);
            userResponse.setRoleNames(user.roleNames());
            userResponse.setStatus(user.getStatus().name());
            if (user.getAddress() != null) {
                Address address = addressRepository.findById(user.getAddress().getAddressId()).orElse(null);
                userResponse.setSpecificAddress(address.getSpecificAddress());
                userResponse.setWardName(address.getWard().getWardName());
                userResponse.setDistrictName(address.getWard().getDistrict().getDistrictName());
                userResponse.setCityName(address.getWard().getDistrict().getCity().getCityName());
            }
            return userResponse;
        }).collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userList);
    }

    @Override
    public ResponseEntity<List<RequestDoctorResponse>> getAllRequestDoctors() {
        List<RequestDoctorResponse> requestList = experienceRepository.findAll().stream().map(experience -> {
            RequestDoctorResponse requestDoctorResponse = ExperienceMapper.EXPERIENCE_MAPPER.mapToRequestDoctorResponse(experience);
            requestDoctorResponse.setUserId(experience.getUser().getUserId());
            requestDoctorResponse.setUserCode(experience.getUser().getUserCode());
            requestDoctorResponse.setEmail(experience.getUser().getEmail());
            requestDoctorResponse.setUniversityName(experience.getUser().getUniversityName());
            requestDoctorResponse.setSkillNames(experience.skillNames());
            requestDoctorResponse.setFileResponses(getAllFiles(requestDoctorResponse.getUserId()).stream().collect(Collectors.toSet()));
            return requestDoctorResponse;
        }).collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(requestList);
    }

    public List<FileResponse> getAllFiles(Long userId) {
        List<FileResponse> fileResponses = fileService.loadFilesByUserId(userId).map(file -> {
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(file.getFileId().toString()).toUriString();
            return new FileResponse(file.getFilePath().split("/")[0], file.getFilePath().split("/")[2], fileUrl);
        }).collect(Collectors.toList());
        return fileResponses;
    }
}
