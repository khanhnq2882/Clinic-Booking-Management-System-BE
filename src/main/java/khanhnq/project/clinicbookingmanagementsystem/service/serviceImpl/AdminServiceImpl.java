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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private final Workbook workbook = new XSSFWorkbook();

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
    public ServicesDTO getServiceById(Long serviceId) {
        Services services = servicesRepository.findById(serviceId).orElse(null);
        return ServicesDTO.builder()
                .serviceId(Objects.requireNonNull(services).getServiceId())
                .serviceCode(services.getServiceCode())
                .serviceName(services.getServiceName())
                .price(services.getPrice())
                .description(services.getDescription())
                .status(services.getStatus().name())
                .serviceCategoryName(services.serviceCategoryName())
                .build();
    }

    @Override
    public String updateService(ServiceRequest serviceRequest, Long serviceId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new ResourceException("You do not have permission to update service category.", HttpStatus.UNAUTHORIZED);
        }
        Services service = servicesRepository.findById(serviceId).orElse(null);
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceRequest.getServiceCategoryId()).orElse(null);
        serviceCode(service, Objects.requireNonNull(serviceCategory));
        Objects.requireNonNull(service).setServiceCategory(serviceCategory);
        service.setServiceName(serviceRequest.getServiceName());
        service.setPrice(serviceRequest.getPrice());
        service.setDescription(serviceRequest.getDescription());
        servicesRepository.save(service);
        return "Update service successfully.";
    }

    @Override
    public List<UserDTO> getUsers() {
        return userRepository.getUsers()
                .stream()
                .map(user -> {
                    UserDTO userDTO = UserMapper.USER_MAPPER.mapToUserDTO(user);
                    userDTO.setUserAddress(getAddress(user));
                    return userDTO;
                })
                .toList();
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
                .serviceCategoryId(Objects.requireNonNull(serviceCategory).getServiceCategoryId())
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
        Objects.requireNonNull(serviceCategory).setSpecialization(specializationRepository.findById(serviceCategoryRequest.getSpecializationId()).orElse(null));
        serviceCategory.setServiceCategoryName(serviceCategoryRequest.getServiceCategoryName());
        serviceCategory.setDescription(serviceCategoryRequest.getDescription());
        serviceCategoryRepository.save(serviceCategory);
        return "Update service category successfully.";
    }

    @Override
    public ByteArrayInputStream exportUsersToExcel(List<UserDTO> users) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Users");
            String[] headers = {"User Code", "Email", "Full Name", "Date Of Birth",
                    "Gender", "Phone Number", "Address", "Status"};
            createHeader(sheet, headers);
            int firstRow = 1;
            for (UserDTO userDTO : users) {
                Row currentRow = sheet.createRow(firstRow++);
                String fullName = userDTO.getFirstName() + " " + userDTO.getLastName();
                AddressResponse userAddress = userDTO.getUserAddress();
                String address = userAddress.getSpecificAddress() + ", " + userAddress.getWardName()
                        + ", " + userAddress.getDistrictName() + ", " + userAddress.getCityName();
                createCell(currentRow, 0, userDTO.getUserCode());
                createCell(currentRow, 1, userDTO.getEmail());
                createCell(currentRow, 2, (Objects.isNull(userDTO.getFirstName()) && Objects.isNull(userDTO.getLastName())) ? " " : fullName);
                createCell(currentRow, 3, Objects.isNull(userDTO.getDateOfBirth()) ? " " : userDTO.getDateOfBirth());
                createCell(currentRow, 4, userDTO.getGender());
                createCell(currentRow, 5, Objects.isNull(userDTO.getPhoneNumber()) ? " " : userDTO.getPhoneNumber());
                createCell(currentRow, 6, Objects.isNull(userDTO.getUserAddress().getSpecificAddress()) ? " " : address);
                createCell(currentRow, 7, userDTO.getStatus());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new ResourceException("Failed export data to file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<ServiceCategory> importServiceCategoriesFromExcel(InputStream inputStream){
        try {
            List<ServiceCategory> serviceCategories = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("service_category");
            Iterator<Row> rows = sheet.iterator();
            int rowIndex = 0;
            while (rows.hasNext()) {
                ServiceCategory serviceCategory = new ServiceCategory();
                Row currentRow = rows.next();
                if (rowIndex == 0) {
                    rowIndex++;
                    continue;
                }
                Iterator<Cell> cells = currentRow.cellIterator();
                int cellIndex = 0;
                while (cells.hasNext()) {
                    Cell currentCell = cells.next();
                    switch (cellIndex) {
                        case 0 -> {
                            if (currentCell.getStringCellValue() == null) {
                                throw new ResourceException("Service category name in row "+rowIndex+" can't be empty.", HttpStatus.BAD_REQUEST);
                            }
                            serviceCategory.setServiceCategoryName(currentCell.getStringCellValue());
                        }
                        case 1 -> {
                            if (currentCell.getStringCellValue() == null) {
                                throw new ResourceException("Description in row "+rowIndex+" can't be empty.", HttpStatus.BAD_REQUEST);
                            }
                            serviceCategory.setDescription(currentCell.getStringCellValue());
                        }
                        case 2 -> {
                            if (currentCell.getStringCellValue() == null) {
                                throw new ResourceException("Specialization name in row "+rowIndex+" can't be empty.", HttpStatus.BAD_REQUEST);
                            }
                            Specialization specialization = specializationRepository.getSpecializationBySpecializationName(currentCell.getStringCellValue());
                            serviceCategory.setSpecialization(specialization);
                        }
                        default -> {
                        }
                    }
                    cellIndex ++;
                }
                serviceCategories.add(serviceCategory);
                rowIndex ++;
            }
            return serviceCategories;
        } catch (IOException ex) {
            throw new ResourceException("Failed to import data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<Services> importServicesFromExcel(InputStream inputStream) {
        return null;
    }

    public void createHeader (Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i=0 ; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            Font font = workbook.createFont();
            font.setBold(true);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
        }
    }

    public void createCell (Row row, int cellIndex, Object value) {
        Cell cell = row.createCell(cellIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            CreationHelper helper = workbook.getCreationHelper();
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(helper.createDataFormat().getFormat("dd-MM-yyyy"));
            cell.setCellValue((Date) value);
            cell.setCellStyle(cellStyle);
        }
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
            addressResponse.setAddressId(Objects.requireNonNull(address).getAddressId());
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
