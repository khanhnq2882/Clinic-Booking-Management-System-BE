package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EServiceStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceException;
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
    public String approveRequestDoctor(Long userId) {
        User currentUser = authService.getCurrentUser();
        User user = userRepository.findById(userId).orElse(null);
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().equals(ERole.ROLE_ADMIN))) {
            throw new ResourceException("You do not have permission to update user roles.", HttpStatus.UNAUTHORIZED);
        }
        Role role = roleRepository.findRoleByRoleName(ERole.ROLE_DOCTOR);
        if (role == null) {
            roleRepository.save(Role.builder().roleName(ERole.ROLE_DOCTOR).build());
        }
        Objects.requireNonNull(user).getRoles().add(role);
        userRepository.save(user);
        return "Update successfully .User "+user.getFirstName()+" "+user.getLastName()+" is became a doctor in the system.";
    }

    @Override
    public String rejectRequestDoctor(Long userId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().equals(ERole.ROLE_ADMIN))) {
            throw new ResourceException("You do not have permission to update user roles.", HttpStatus.UNAUTHORIZED);
        }
        Map<Long, List<Experience>> experiences = groupExperiencesByUserId();
        for (Experience experience : experiences.get(userId)) {
            experienceRepository.deleteExperiencesSkills(experience.getExperienceId());
            experienceRepository.deleteExperiences(experience.getExperienceId());
        }
        return "Reject request successfully.";
    }

    @Override
    public UserResponse getAllUsers(int page, int size, String[] sorts) {
        Page<User> userPage = userRepository.getAllUsers(pagingSort(page, size, sorts));
        List<UserDTO> users = userPage.getContent()
                .stream()
                .map(user -> {
                    UserDTO userDTO = UserMapper.USER_MAPPER.mapToUserDTO(user);
                    userDTO.setUserAddress(getAddress(user));
                    return userDTO;
                }).collect(Collectors.toList());
        return UserResponse.builder()
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
            User user = userRepository.findById(userId).orElse(null);
            UserMapper.USER_MAPPER.mapToRequestDoctorResponse(requestDoctorResponse, user);
            List<ExperienceDTO> experiences = groupExperiencesByUserId().get(userId).stream().map(experience -> {
                ExperienceDTO experienceDTO = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperienceResponse(experience);
                experienceDTO.setSkillNames(experience.skillNames());
                return experienceDTO;
            }).collect(Collectors.toList());
            requestDoctorResponse.setDoctorExperiences(experiences);
            requestDoctorResponse.setRoleNames(Objects.requireNonNull(user).roleNames());
            getMedicalLicenseDegree(requestDoctorResponse, userId);
            requestList.add(requestDoctorResponse);
        }
        return requestList;
    }

    @Override
    public DoctorResponse getAllDoctors(int page, int size, String[] sorts) {
        Page<User> doctorPage = userRepository.getAllDoctors(pagingSort(page, size, sorts));
        List<DoctorDTO> doctors = doctorPage.getContent().stream().map(user -> {
                    DoctorDTO doctorDTO = UserMapper.USER_MAPPER.mapToDoctorResponse(user);
                    if (user.getSpecialization() != null) {
                        doctorDTO.setSpecializationName(user.specializationName());
                    }
                    doctorDTO.setDoctorAddress(getAddress(user));
                    return doctorDTO;
                }).collect(Collectors.toList());
        return DoctorResponse.builder()
                .totalItems(doctorPage.getTotalElements())
                .totalPages(doctorPage.getTotalPages())
                .currentPage(doctorPage.getNumber())
                .doctors(doctors)
                .build();
    }

    @Override
    public String addServiceCategory(ServiceCategoryRequest serviceCategoryRequest) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new ResourceException("You do not have permission to add service category.", HttpStatus.UNAUTHORIZED);
        }
        Specialization specialization = specializationRepository.findById(serviceCategoryRequest.getSpecializationId()).orElse(null);
        ServiceCategory serviceCategory = ServiceCategoryMapper.SERVICE_CATEGORY_MAPPER.mapToServiceCategory(serviceCategoryRequest);
        serviceCategory.setSpecialization(specialization);
        serviceCategoryRepository.save(serviceCategory);
        return "Add service category successfully.";
    }

    @Override
    public String addService(ServiceRequest serviceRequest) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new ResourceException("You do not have permission to add service.", HttpStatus.UNAUTHORIZED);
        }
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceRequest.getServiceCategoryId()).orElse(null);
        Services services = ServicesMapper.SERVICES_MAPPER.mapToServices(serviceRequest);
        services.setStatus(EServiceStatus.ACTIVE);
        services.setServiceCategory(serviceCategory);
        serviceCode(services, Objects.requireNonNull(serviceCategory));
        servicesRepository.save(services);
        return "Add service successfully.";
    }

    @Override
    public List<SpecializationResponse> getAllSpecializations() {
        return specializationRepository.findAll()
                .stream()
                .map(specialization -> SpecializationResponse.builder()
                        .specializationId(specialization.getSpecializationId())
                        .specializationName(specialization.getSpecializationName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceCategoryDTO> getServiceCategories(Long specializationId) {
        return serviceCategoryRepository.getServiceCategoriesBySpecializationId(specializationId)
                .stream()
                .map(serviceCategory -> ServiceCategoryDTO.builder()
                        .serviceCategoryId(serviceCategory.getServiceCategoryId())
                        .serviceCategoryName(serviceCategory.getServiceCategoryName())
                        .specializationId(specializationId)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ServiceCategoryResponse getAllServiceCategories(int page, int size, String[] sorts) {
        Page<ServiceCategory> serviceCategoryPage = serviceCategoryRepository.findAll(pagingSort(page, size, sorts));
        List<ServiceCategoryDTO> serviceCategories = serviceCategoryPage.getContent()
                .stream()
                .map(serviceCategory -> ServiceCategoryDTO.builder()
                        .serviceCategoryId(serviceCategory.getServiceCategoryId())
                        .serviceCategoryName(serviceCategory.getServiceCategoryName())
                        .description(serviceCategory.getDescription())
                        .specializationId(serviceCategory.getSpecialization().getSpecializationId())
                        .specializationName(serviceCategory.getSpecialization().getSpecializationName())
                        .build())
                .collect(Collectors.toList());
        return ServiceCategoryResponse.builder()
                .totalItems(serviceCategoryPage.getTotalElements())
                .totalPages(serviceCategoryPage.getTotalPages())
                .currentPage(serviceCategoryPage.getNumber())
                .serviceCategories(serviceCategories)
                .build();
    }

    @Override
    public ServicesResponse getAllServices(int page, int size, String[] sorts) {
        Page<Services> servicesPage = servicesRepository.findAll(pagingSort(page, size, sorts));
        List<ServicesDTO> servicesResponses = servicesPage.getContent()
                .stream()
                .map(services -> {
                    ServicesDTO servicesResponse = ServicesMapper.SERVICES_MAPPER.mapToServicesResponse(services);
                    servicesResponse.setServiceCategoryName(services.serviceCategoryName());
                    return servicesResponse;
                }).collect(Collectors.toList());
        return ServicesResponse.builder()
                .totalItems(servicesPage.getTotalElements())
                .totalPages(servicesPage.getTotalPages())
                .currentPage(servicesPage.getNumber())
                .services(servicesResponses)
                .build();
    }

    @Override
    public ServiceCategoryDTO getServiceCategoryById(Long serviceCategoryId) {
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId).orElse(null);
        return ServiceCategoryDTO.builder()
                .serviceCategoryId(serviceCategory.getServiceCategoryId())
                .serviceCategoryName(serviceCategory.getServiceCategoryName())
                .description(serviceCategory.getDescription())
                .specializationId(serviceCategory.getSpecialization().getSpecializationId())
                .specializationName(serviceCategory.getSpecialization().getSpecializationName())
                .build();
    }

    @Override
    public String updateServiceCategory(ServiceCategoryRequest serviceCategoryRequest, Long serviceCategoryId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new ResourceException("You do not have permission to update service category.", HttpStatus.UNAUTHORIZED);
        }
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId).orElse(null);
        serviceCategory.setSpecialization(specializationRepository.findById(serviceCategoryRequest.getSpecializationId()).orElse(null));
        serviceCategory.setServiceCategoryName(serviceCategoryRequest.getServiceCategoryName());
        serviceCategory.setDescription(serviceCategoryRequest.getDescription());
        serviceCategoryRepository.save(serviceCategory);
        return "Update service category successfully.";
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
    
    public void serviceCode(Services services, ServiceCategory serviceCategory) {
        StringBuilder code = new StringBuilder(" ");
        for (String s : serviceCategory.getServiceCategoryName().split(" ")) {
            code.append(s.charAt(0));
        }
        List<Services> servicesList = servicesRepository.getServicesByCode(code.toString());
        if (servicesList.size() == 0) {
            services.setServiceCode(code + "1");
        } else {
            String s = code.toString();
            Long maxServiceCode = Collections.max(servicesList
                    .stream()
                    .map(service -> Long.parseLong(service.getServiceCode().substring(s.length())))
                    .toList());
            services.setServiceCode(code.toString() +(maxServiceCode+1));
        }
    }

    public List<FileResponse> getAllFiles(Long userId) {
        return fileService.loadFilesByUserId(userId).map(file -> {
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(file.getFileId().toString()).toUriString();
            return new FileResponse(file.getFilePath().split("/")[1], file.getFilePath().split("/")[2], fileUrl);
        }).collect(Collectors.toList());
    }

    public Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    public Pageable pagingSort(int page, int size, String[] sorts) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sorts[0].contains(",")) {
            for (String sortOrder : sorts) {
                String[] sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
            }
        } else {
            orders.add(new Sort.Order(getSortDirection(sorts[1]), sorts[0]));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }
}
