package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.UserDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EServiceStatus;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ServiceCategoryMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.ServicesMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final FileService fileService;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final ExperienceRepository experienceRepository;
    private final SpecializationRepository specializationRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServicesRepository servicesRepository;

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
    public ResponseEntity<String> rejectRequestDoctor(Long userId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().filter(role -> role.getRoleName().equals(ERole.ROLE_ADMIN)).findAny().isPresent()) {
            Map<Long, List<Experience>> experiences = groupExperiencesByUserId();
            for (Experience experience : experiences.get(userId)) {
                experienceRepository.deleteExperiencesSkills(experience.getExperienceId());
                experienceRepository.deleteExperiences(experience.getExperienceId());
            }

            // dung constant
            return MessageResponse.getResponseMessage("Reject request successfully.", HttpStatus.OK);
        }
        // constant
        return MessageResponse.getResponseMessage("You do not have permission to update user roles.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public UserPageResponse getAllUsers(int page, int size, String[] sorts) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sorts[0].contains(",")) {
            for (String sortOrder : sorts) {
                String[] sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
            }
        } else {
            orders.add(new Sort.Order(getSortDirection(sorts[1]), sorts[0]));
        }
        Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
        Page<User> userPage = userRepository.getAllUsers(pagingSort);
        List<UserResponse> users = userPage.getContent().stream().map(user -> {
            UserResponse userDTO = UserMapper.USER_MAPPER.mapToUserResponse(user);
            userDTO.setUserAddress(getAddress(user));
            return userDTO;
        }).collect(Collectors.toList());
        return new UserPageResponse().builder()
                .totalItems(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .users(users)
                .build();
    }

    @Override
    public List<RequestDoctorResponse> getAllRequestDoctors() {
        List<RequestDoctorResponse> requestList = new ArrayList<>();
        for (Long userId : groupExperiencesByUserId().keySet()) {
            RequestDoctorResponse requestDoctorResponse = new RequestDoctorResponse();

            // phai sua luon
            User user = userRepository.findById(userId).orElse(null);

            UserMapper.USER_MAPPER.mapToRequestDoctorResponse(requestDoctorResponse, user);
            List<ExperienceResponse> experiences = groupExperiencesByUserId().get(userId).stream().map(experience -> {
                ExperienceResponse experienceResponse = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperienceResponse(experience);
                experienceResponse.setSkillNames(experience.skillNames());
                return experienceResponse;
            }).collect(Collectors.toList());
            requestDoctorResponse.setDoctorExperiences(experiences);
//            requestDoctorResponse.setRoleNames(user.roleNames());
            getMedicalLicenseDegree(requestDoctorResponse, userId);
            requestList.add(requestDoctorResponse);
        }
        return requestList;
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

    @Override
    public ResponseEntity<String> addServiceCategory(ServiceCategoryRequest serviceCategoryRequest) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().filter(role -> role.getRoleName().name().equals("ROLE_ADMIN")).findAny().isPresent()) {
            Specialization specialization = specializationRepository.findById(serviceCategoryRequest.getSpecializationId()).orElse(null);
            ServiceCategory serviceCategory = ServiceCategoryMapper.SERVICE_CATEGORY_MAPPER.mapToServiceCategory(serviceCategoryRequest);
            serviceCategory.setSpecialization(specialization);
            serviceCategoryRepository.save(serviceCategory);
            return MessageResponse.getResponseMessage("Add service category successfully.", HttpStatus.OK);
        }
        return MessageResponse.getResponseMessage("You do not have permission to add service category.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<String> addService(ServiceRequest serviceRequest) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().filter(role -> role.getRoleName().name().equals("ROLE_ADMIN")).findAny().isPresent()) {
            ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceRequest.getServiceCategoryId()).orElse(null);
            Services services = ServicesMapper.SERVICES_MAPPER.mapToServices(serviceRequest);
            services.setStatus(EServiceStatus.ACTIVE);
            services.setServiceCategory(serviceCategory);
            serviceCode(services, serviceCategory);
            servicesRepository.save(services);
            return MessageResponse.getResponseMessage("Add service successfully.", HttpStatus.OK);
        }
        return MessageResponse.getResponseMessage("You do not have permission to add service.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<SpecializationResponse>> getAllSpecializations() {
        List<SpecializationResponse> specializations = specializationRepository.findAll()
                .stream()
                .map(specialization -> SpecializationResponse.builder()
                        .specializationId(specialization.getSpecializationId())
                        .specializationName(specialization.getSpecializationName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(specializations);
    }

    @Override
    public ResponseEntity<List<ServiceCategoryResponse>> getAllServiceCategories(Long specializationId) {
        List<ServiceCategoryResponse> serviceCategories = serviceCategoryRepository.getServiceCategoriesBySpecializationId(specializationId)
                .stream()
                .map(serviceCategory -> ServiceCategoryResponse.builder()
                        .serviceCategoryId(serviceCategory.getServiceCategoryId())
                        .serviceCategoryName(serviceCategory.getServiceCategoryName())
                        .specializationId(specializationId)
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(serviceCategories);
    }

    @Override
    public ResponseEntity<List<ServicesResponse>> getAllServices() {
        List<ServicesResponse> servicesResponses = servicesRepository.findAll()
                .stream()
                .map(services -> {
                    ServicesResponse servicesResponse = ServicesMapper.SERVICES_MAPPER.mapToServicesResponse(services);
                    servicesResponse.setServiceCategoryName(services.serviceCategoryName());
                    return servicesResponse;
                }).collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(servicesResponses);
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
            // can hoc them mapper
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
    
    public void serviceCode(Services services, ServiceCategory serviceCategory) {
        String code = " ";
        for (String s : serviceCategory.getServiceCategoryName().split(" ")) {
            code += s.substring(0,1);
        }
        List<Services> servicesList = servicesRepository.getServicesByCode(code);
        if (servicesList.size() == 0) {
            services.setServiceCode(code + "1");
        } else {
            String s = code;
            Long maxServiceCode = Collections.max(servicesList
                    .stream()
                    .map(service -> Long.parseLong(service.getServiceCode().substring(s.length())))
                    .collect(Collectors.toList()));
            services.setServiceCode(code+(maxServiceCode+1));
        }
    }

    public List<FileResponse> getAllFiles(Long userId) {
        // tim hieu java NIO -> try with resource
        List<FileResponse> fileResponses = fileService.loadFilesByUserId(userId).map(file -> {
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(file.getFileId().toString()).toUriString();
            return new FileResponse(file.getFilePath().split("/")[1], file.getFilePath().split("/")[2], fileUrl);
        }).collect(Collectors.toList());
        return fileResponses;
    }

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }
}
