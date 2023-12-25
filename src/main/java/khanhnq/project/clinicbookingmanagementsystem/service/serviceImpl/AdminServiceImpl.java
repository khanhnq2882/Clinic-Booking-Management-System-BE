package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.mapper.*;
import khanhnq.project.clinicbookingmanagementsystem.service.common.MethodsCommon;
import khanhnq.project.clinicbookingmanagementsystem.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ERole;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EServiceStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceException;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final RoleRepository roleRepository;
    private final ExperienceRepository experienceRepository;
    private final SpecializationRepository specializationRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServicesRepository servicesRepository;
    private final BookingRepository bookingRepository;
    private final MethodsCommon methodsCommon;
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
        return "Update successfully .User " + user.getFirstName() + " " + user.getLastName() + " is became a doctor in the system.";
    }

    @Override
    public String rejectRequestDoctor(Long userId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().equals(ERole.ROLE_ADMIN))) {
            throw new ResourceException("You do not have permission to update user roles.", HttpStatus.UNAUTHORIZED);
        }
        Map<Long, List<Experience>> experiences = methodsCommon.groupExperiencesByUserId();
        for (Experience experience : experiences.get(userId)) {
            experienceRepository.deleteExperiencesSkills(experience.getExperienceId());
            experienceRepository.deleteExperiences(experience.getExperienceId());
        }
        return "Reject request successfully.";
    }

    @Override
    public UserResponse getAllUsers(int page, int size, String[] sorts) {
        Page<User> userPage = userRepository.getAllUsers(methodsCommon.pagingSort(page, size, sorts));
        List<UserDTO> users = userPage.getContent()
                .stream()
                .map(user -> {
                    UserDTO userDTO = UserMapper.USER_MAPPER.mapToUserDTO(user);
                    userDTO.setUserAddress(methodsCommon.getAddress(user));
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
        for (Long userId : methodsCommon.groupExperiencesByUserId().keySet()) {
            RequestDoctorResponse requestDoctorResponse = new RequestDoctorResponse();
            User user = userRepository.findById(userId).orElse(null);
            UserMapper.USER_MAPPER.mapToRequestDoctorResponse(requestDoctorResponse, user);
            List<ExperienceDTO> experiences = methodsCommon.groupExperiencesByUserId().get(userId).stream().map(experience -> {
                ExperienceDTO experienceDTO = ExperienceMapper.EXPERIENCE_MAPPER.mapToExperienceResponse(experience);
                experienceDTO.setSkillNames(experience.skillNames());
                return experienceDTO;
            }).collect(Collectors.toList());
            requestDoctorResponse.setDoctorExperiences(experiences);
            requestDoctorResponse.setRoleNames(Objects.requireNonNull(user).roleNames());
            methodsCommon.getMedicalLicenseDegree(requestDoctorResponse, userId);
            requestList.add(requestDoctorResponse);
        }
        return requestList;
    }

    @Override
    public DoctorResponse getAllDoctors(int page, int size, String[] sorts) {
        Page<User> doctorPage = userRepository.getAllDoctors(methodsCommon.pagingSort(page, size, sorts));
        List<DoctorDTO> doctors = doctorPage.getContent().stream().map(user -> {
            DoctorDTO doctorDTO = UserMapper.USER_MAPPER.mapToDoctorResponse(user);
            if (user.getSpecialization() != null) {
                doctorDTO.setSpecializationName(user.specializationName());
            }
            doctorDTO.setDoctorAddress(methodsCommon.getAddress(user));
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
        methodsCommon.serviceCode(services, Objects.requireNonNull(serviceCategory));
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
        methodsCommon.serviceCode(service, Objects.requireNonNull(serviceCategory));
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
                    userDTO.setUserAddress(methodsCommon.getAddress(user));
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
        Page<ServiceCategory> serviceCategoryPage = serviceCategoryRepository.findAll(methodsCommon.pagingSort(page, size, sorts));
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
        Page<Services> servicesPage = servicesRepository.findAll(methodsCommon.pagingSort(page, size, sorts));
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
            methodsCommon.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (UserDTO userDTO : users) {
                Row currentRow = sheet.createRow(firstRow++);
                String fullName = userDTO.getFirstName() + " " + userDTO.getLastName();
                AddressResponse userAddress = userDTO.getUserAddress();
                String address = userAddress.getSpecificAddress() + ", " + userAddress.getWardName() + ", " + userAddress.getDistrictName() + ", " + userAddress.getCityName();
                methodsCommon.createCell(workbook, currentRow, 0, userDTO.getUserCode());
                methodsCommon.createCell(workbook, currentRow, 1, userDTO.getEmail());
                methodsCommon.createCell(workbook, currentRow, 2, (Objects.isNull(userDTO.getFirstName()) && Objects.isNull(userDTO.getLastName())) ? " " : fullName);
                methodsCommon.createCell(workbook, currentRow, 3, Objects.isNull(userDTO.getDateOfBirth()) ? " " : userDTO.getDateOfBirth());
                methodsCommon.createCell(workbook, currentRow, 4, userDTO.getGender() == 1 ? "Male" : "Female");
                methodsCommon.createCell(workbook, currentRow, 5, Objects.isNull(userDTO.getPhoneNumber()) ? " " : userDTO.getPhoneNumber());
                methodsCommon.createCell(workbook, currentRow, 6, Objects.isNull(userDTO.getUserAddress().getSpecificAddress()) ? " " : address);
                methodsCommon.createCell(workbook, currentRow, 7, userDTO.getStatus());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new ResourceException("Failed export data to file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<ServiceCategory> importServiceCategoriesFromExcel(InputStream inputStream) {
        try {
            List<ServiceCategory> serviceCategories = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("service category");
            if (sheet == null) {
                throw new ResourceException("Sheet named 'service category' doesn't not exist.", HttpStatus.BAD_REQUEST);
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                ServiceCategory serviceCategory = new ServiceCategory();
                List<Cell> cells = methodsCommon.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    methodsCommon.checkBlankType(cells.get(indexCell), indexRow, indexCell);
                    switch (indexCell) {
                        case 0 -> {
                            String serviceCategoryName = methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue();
                            List<String> serviceCategoriesName = serviceCategoryRepository.findAll().stream().map(ServiceCategory::getServiceCategoryName).toList();
                            for (String name : serviceCategoriesName) {
                                if (serviceCategoryName.equals(name)) {
                                    throw new ResourceException("Import data failed. Service category named '" + serviceCategoryName + "' is already existed.", HttpStatus.BAD_REQUEST);
                                }
                            }
                            serviceCategory.setServiceCategoryName(serviceCategoryName);
                        }
                        case 1 ->
                                serviceCategory.setDescription(methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue());
                        case 2 -> {
                            String specializationName = methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue();
                            Specialization specialization = specializationRepository.getSpecializationBySpecializationName(specializationName);
                            if (Objects.isNull(specialization)) {
                                throw new ResourceException("Import failed. Specialization name in row " + indexRow + " is not exist.", HttpStatus.BAD_REQUEST);
                            }
                            serviceCategory.setSpecialization(specialization);
                        }
                        default -> {
                        }
                    }
                }
                serviceCategories.add(serviceCategory);
            }
            return serviceCategories;
        } catch (IOException e) {
            throw new ResourceException("Failed to import data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<Services> importServicesFromExcel(InputStream inputStream) {
        try {
            List<Services> services = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("services");
            if (sheet == null) {
                throw new ResourceException("Sheet named 'services' doesn't not exist.", HttpStatus.BAD_REQUEST);
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                Services service = new Services();
                List<Cell> cells = methodsCommon.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    methodsCommon.checkBlankType(cells.get(indexCell), indexRow, indexCell);
                    switch (indexCell) {
                        case 0 -> {
                            String serviceName = methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue();
                            List<String> servicesName = servicesRepository.findAll().stream().map(Services::getServiceName).toList();
                            for (String name : servicesName) {
                                if (serviceName.equals(name)) {
                                    throw new ResourceException("Import data failed. Service named '" + serviceName + "' is already existed.", HttpStatus.BAD_REQUEST);
                                }
                            }
                            service.setServiceName(serviceName);
                        }
                        case 1 ->
                                service.setPrice(methodsCommon.checkNumericType(cells.get(indexCell), indexRow, indexCell).getNumericCellValue());
                        case 2 ->
                                service.setDescription(methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue());
                        case 3 -> {
                            String serviceStatus = methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue();
                            if (!serviceStatus.equals("ACTIVE") && !serviceStatus.equals("SUSPENDED") && !serviceStatus.equals("INACTIVE")) {
                                throw new ResourceException("Import data failed. The value of column " + (indexCell + 1) + " , row " + indexRow + " must be 'ACTIVE' or 'SUSPENDED' or 'INACTIVE'.", HttpStatus.BAD_REQUEST);
                            }
                            service.setStatus(EServiceStatus.valueOf(serviceStatus));
                        }
                        case 4 -> {
                            ServiceCategory serviceCategory = serviceCategoryRepository.getServiceCategoriesByServiceCategoryName(methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue());
                            if (Objects.isNull(serviceCategory)) {
                                throw new ResourceException("Import failed. Service category name in row " + indexRow + " is not exist.", HttpStatus.BAD_REQUEST);
                            }
                            methodsCommon.serviceCode(service, serviceCategory);
                            service.setServiceCategory(serviceCategory);
                        }
                        default -> {
                        }
                    }
                }
                services.add(service);
            }
            return services;
        } catch (IOException e) {
            throw new ResourceException("Failed to import data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<Booking> importBookingsFromExcel(InputStream inputStream) {
        try {
            List<Booking> bookings = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("bookings");
            if (sheet == null) {
                throw new ResourceException("Sheet named 'bookings' doesn't not exist.", HttpStatus.BAD_REQUEST);
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                Booking booking = new Booking();
                List<Cell> cells = methodsCommon.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    methodsCommon.checkBlankType(cells.get(indexCell), indexRow, indexCell);
                    switch (indexCell) {
                        case 0 -> {
                            String fullName = methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue();
                            booking.setFirstName(fullName.split(" ")[0]);
                            booking.setLastName(fullName.substring(fullName.split(" ")[0].length() + 1));
                        }
                        case 1 -> {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date dateOfBirth = methodsCommon.checkDateType(cells.get(indexCell), indexRow, indexCell).getDateCellValue();
                            if (!dateFormat.format(dateOfBirth).matches("^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$")) {
                                throw new ResourceException("Import failed. Date of birth in row " + (indexRow + 1) + " must be yyyy-MM-dd format.", HttpStatus.BAD_REQUEST);
                            }
                            booking.setDateOfBirth(dateOfBirth);
                        }
                        case 2 -> {
                            String gender = methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue();
                            if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")) {
                                throw new ResourceException("Import data failed. The value of column " + (indexCell + 1) + " , row " + indexRow + " must be 'Male' or 'Female'.", HttpStatus.BAD_REQUEST);
                            }
                            booking.setGender(gender.equalsIgnoreCase("Male") ? 1 : 0);
                        }
                        case 3 ->
                                booking.setPhoneNumber(methodsCommon.getPhoneNumberFromExcel(cells.get(indexCell), indexRow, indexCell));
                        case 4 ->
                                booking.setAddress(methodsCommon.getAddressFromExcel(cells.get(indexCell), indexRow, indexCell));
                        case 6 -> {
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date appointmentDate = methodsCommon.checkDateType(cells.get(indexCell), indexRow, indexCell).getDateCellValue();
                            if (!dateFormat.format(appointmentDate).matches("^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$")) {
                                throw new ResourceException("Import failed. Appointment date in row " + (indexRow + 1) + " must be yyyy-MM-dd format.", HttpStatus.BAD_REQUEST);
                            }
                            booking.setAppointmentDate(methodsCommon.checkDateType(cells.get(indexCell), indexRow, indexCell).getDateCellValue());
                        }
                        case 7 -> {
                            Specialization specialization = specializationRepository.getSpecializationBySpecializationName(cells.get(indexCell - 2).getStringCellValue());
                            if (Objects.isNull(specialization)) {
                                throw new ResourceException("Import failed. Specialization name in row " + (indexRow + 1) + " is not exist.", HttpStatus.BAD_REQUEST);
                            }
                            Date appointmentDate = cells.get(indexCell - 1).getDateCellValue();
                            LocalTime startTime = cells.get(indexCell).getLocalDateTimeCellValue().toLocalTime();
                            LocalTime endTime = cells.get(indexCell + 1).getLocalDateTimeCellValue().toLocalTime();
                            List<User> doctors = methodsCommon.groupDoctorsBySpecialization().get(specialization.getSpecializationId())
                                    .stream()
                                    .filter(doctor -> methodsCommon.groupWorkScheduleByDoctor().get(doctor.getUserId())
                                            .stream()
                                            .anyMatch(workSchedule -> startTime.equals(workSchedule.getStartTime()) && endTime.equals(workSchedule.getEndTime())))
                                    .toList();
                            for (User doctor : doctors) {
                                for (WorkSchedule workSchedule : methodsCommon.groupWorkScheduleByDoctor().get(doctor.getUserId())) {
                                    for (Booking b : bookingRepository.getBookingsByDoctorId(doctor.getUserId())) {
                                        if (b.getAppointmentDate().equals(appointmentDate) && b.getWorkSchedule().getStartTime().equals(workSchedule.getStartTime())) {
                                            throw new ResourceException("You cannot import data in row " + (indexRow + 1) + " because all doctors have appointments at "
                                                    + workSchedule.getStartTime() + " - " + workSchedule.getEndTime() + " on " + new SimpleDateFormat("dd/MM/yyyy").format(appointmentDate), HttpStatus.BAD_REQUEST);
                                        }
                                        if (startTime.equals(workSchedule.getStartTime()) && endTime.equals(workSchedule.getEndTime())) {
                                            booking.setWorkSchedule(workSchedule);
                                        } else {
//                                            throw new ResourceException("Invalid data in row " + (indexRow + 1) + " . Currently there are no doctors available to schedule examinations from "
//                                                    + workSchedule.getStartTime() + " to " + workSchedule.getEndTime() + ". Please contact the booking person and choose another time.", HttpStatus.BAD_REQUEST);
                                        }
                                    }
                                }
                            }
                        }
                        case 9 ->
                                booking.setDescribeSymptoms(methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue());
                        case 10 -> {
                            String serviceStatus = methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue();
                            if (!serviceStatus.equals("PENDING") && !serviceStatus.equals("CONFIRMED") && !serviceStatus.equals("CANCELLED") && !serviceStatus.equals("COMPLETED")) {
                                throw new ResourceException("Import data failed. The value of column " + (indexCell + 1) + " , row " + indexRow + " must be 'PENDING' or 'CONFIRMED' or 'CANCELLED' or 'COMPLETED'.", HttpStatus.BAD_REQUEST);
                            }
                            booking.setStatus(EBookingStatus.valueOf(methodsCommon.checkStringType(cells.get(indexCell), indexRow, indexCell).getStringCellValue()));
                        }
                        default -> {
                        }
                    }
                }
                booking.setBookingCode(methodsCommon.bookingCode());
                bookings.add(booking);
            }
            return bookings;
        } catch (IOException e) {
            throw new ResourceException("Failed to import data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll()
                .stream()
                .filter(booking -> Objects.isNull(booking.getUser()) && Objects.isNull(booking.getWorkSchedule()))
                .toList();
        List<BookingDTO> bookingDTOS = bookings.stream()
                .map(booking -> {
                    BookingDTO bookingDTO = BookingMapper.BOOKING_MAPPER.mapToBookingDTO(booking);
                    bookingDTO.setUserAddress(methodsCommon.getAddress(booking));
                    return bookingDTO;
                }).toList();
        return bookingDTOS;
    }
}
