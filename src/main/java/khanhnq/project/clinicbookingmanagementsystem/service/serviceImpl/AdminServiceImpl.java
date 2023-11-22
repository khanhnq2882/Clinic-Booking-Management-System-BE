package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.Address;
import khanhnq.project.clinicbookingmanagementsystem.entity.Experience;
import khanhnq.project.clinicbookingmanagementsystem.entity.Role;
import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<String> approveRequestDoctor(Long userId) {
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
        return MessageResponse.getResponseMessage("You do not have permission to update user roles.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> userList = userRepository.getAllUsers().stream().map(user -> {
            UserResponse userResponse = UserMapper.USER_MAPPER.mapToUserResponse(user);
            userResponse.setRoleNames(user.roleNames());
            userResponse.setStatus(user.getStatus().name());
            userResponse.setUserAddress(getAddress(user));
            return userResponse;
        }).collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userList);
    }

    @Override
    public ResponseEntity<List<RequestDoctorResponse>> getAllRequestDoctors() {
        List<RequestDoctorResponse> requestList = new ArrayList<>();
        for (Long userId : groupExperiencesByUserId().keySet()) {
            RequestDoctorResponse requestDoctorResponse = new RequestDoctorResponse();
            User user = userRepository.findById(userId).orElse(null);
            UserMapper.USER_MAPPER.mapToRequestDoctorResponse(requestDoctorResponse, user);
            List<ExperienceResponse> experiences = groupExperiencesByUserId().get(userId).stream().map(experience -> {
                ExperienceResponse experienceResponse = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperienceResponse(experience);
                experienceResponse.setSkillNames(experience.skillNames());
                return experienceResponse;
            }).collect(Collectors.toList());
            requestDoctorResponse.setDoctorExperiences(experiences);
            requestDoctorResponse.setRoleNames(user.roleNames());
            getMedicalLicenseDegree(requestDoctorResponse, userId);
            requestList.add(requestDoctorResponse);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(requestList);
    }

    @Override
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        List<DoctorResponse> responseList = userRepository.getAllDoctors()
                .stream()
                .map(user -> {
                    DoctorResponse doctorResponse = UserMapper.USER_MAPPER.mapToDoctorResponse(user);
                    if (user.getSpecialization() != null) {
                        doctorResponse.setSpecializationName(user.specializationName());
                    }
                    doctorResponse.setDoctorAddress(getAddress(user));
                    return doctorResponse;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseList);
    }

    public Map<Long, List<Experience>> groupExperiencesByUserId() {
        Map<Long, List<Experience>> map = new HashMap<>();
        for (Experience experience : experienceRepository.findAll()) {
            if (!map.containsKey(experience.getUser().getUserId())) {
                List<Experience> experiences = experienceRepository.getExperiencesByUserId(experience.getUser().getUserId());
                map.put(experience.getUser().getUserId(), experiences);
            }
        }
        return map;
    }

    public AddressResponse getAddress(User user) {
        AddressResponse addressResponse = new AddressResponse();
        if (user.getAddress() != null) {
            Address address = addressRepository.findById(user.getAddress().getAddressId()).orElse(null);
            addressResponse.setAddressId(address.getAddressId());
            addressResponse.setSpecificAddress(address.getSpecificAddress());
            addressResponse.setWardName(address.getWard().getWardName());
            addressResponse.setDistrictName(address.getWard().getDistrict().getDistrictName());
            addressResponse.setCityName(address.getWard().getDistrict().getCity().getCityName());
        }
        return addressResponse;
    }

    public void getMedicalLicenseDegree(RequestDoctorResponse requestDoctorResponse, Long userId) {
        for (FileResponse fileResponse : getAllFiles(userId)) {
            if (fileResponse.getFileType().equals("medical-degree")) {
                requestDoctorResponse.setMedicalDegreeType(fileResponse.getFileType());
                requestDoctorResponse.setMedicalDegreeName(fileResponse.getFileName());
                requestDoctorResponse.setMedicalDegreeUrl(fileResponse.getFileUrl());
            } else {
                requestDoctorResponse.setMedicalLicenseType(fileResponse.getFileType());
                requestDoctorResponse.setMedicalLicenseName(fileResponse.getFileName());
                requestDoctorResponse.setMedicalLicenseUrl(fileResponse.getFileUrl());
            }
        }
    }

    public List<FileResponse> getAllFiles(Long userId) {
        List<FileResponse> fileResponses = fileService.loadFilesByUserId(userId).map(file -> {
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(file.getFileId().toString()).toUriString();
            return new FileResponse(file.getFilePath().split("/")[1], file.getFilePath().split("/")[2], fileUrl);
        }).collect(Collectors.toList());
        return fileResponses;
    }
}
