package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.common.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.*;
import khanhnq.project.clinicbookingmanagementsystem.exception.*;
import khanhnq.project.clinicbookingmanagementsystem.mapper.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.request.TestPackageRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.*;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.security.services.BruteForceProtectionService;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SpecializationRepository specializationRepository;
    private final ServicesRepository servicesRepository;
    private final BookingRepository bookingRepository;
    private final DoctorRepository doctorRepository;
    private final FileRepository fileRepository;
    private final TestPackageRepository testPackageRepository;
    private final TestPackageAttributeRepository testPackageAttributeRepository;
    private final NormalRangeRepository normalRangeRepository;
    private final CommonServiceImpl commonServiceImpl;
    private final Workbook workbook = new XSSFWorkbook();
    private final JavaMailSender mailSender;
    private PasswordEncoder passwordEncoder;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final AuthService authService;

    @Override
    public ResponseEntityBase resetPassword(String email) throws MessagingException {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User user = userRepository.findUserByEmail(email);
        if (Objects.nonNull(user)) {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress("quockhanhnguyen2882@gmail.com"));
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("Your password has been changed.");
            user.setPassword(randomPassword());
            String htmlContent =
                    "<body>" +
                            "<p>Your password has been changed. New password is <b>"+user.getPassword()+"</b>.</p>" +
                            "<p>Regards, <br/><em>Admin Teams</em></p>" +
                            "</body>";
            message.setContent(htmlContent, "text/html; charset=utf-8");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            mailSender.send(message);
            userRepository.save(user);
            response.setData(MessageConstants.RESET_PASSWORD_SUCCESS);
            return response;
        } else {
            throw new ResourceNotFoundException("Email", email);
        }
    }

    @Override
    public ResponseEntityBase unlockAccount(String username) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        User user = userRepository.findUserByUsername(username);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("Account with username '" +username+ "' is not found.");
        }
        if (user.getStatus() != null && user.getStatus().equals(EUserStatus.BANNED)) {
            bruteForceProtectionService.unlockAccount(username);
            user.setStatus(EUserStatus.ACTIVE);
            user.setUpdatedBy(currentUser.getUsername());
            userRepository.save(user);
        }
        response.setData(MessageConstants.UNLOCK_ACCOUNT_SUCCESSFULLY);
        return response;
    }

    @Override
    public ResponseEntityBase getAllUsers(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Page<User> userPage = userRepository.getAllUsers(commonServiceImpl.pagingSort(page, size, sorts));
        List<UserDTO> users = userPage.getContent().stream()
                .map(user -> {
                    UserDTO userDTO = UserMapper.USER_MAPPER.mapToUserDTO(user);
                    if (user.getAddress() != null) {
                        userDTO.setUserAddress(commonServiceImpl.getAddress(user));
                    }
                    String gender = user.getGender() == 1 ? "Male" : "Female";
                    userDTO.setGender(gender);
                    if (userDTO.getDateOfBirth() != null) {
                        DateTimeFormatter dobFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        LocalDate dob = LocalDate.parse(user.getDateOfBirth().toString());
                        userDTO.setDateOfBirth(dob.format(dobFormatter));
                    }
                    DateTimeFormatter createdAtFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    LocalDateTime createdAt = LocalDateTime.parse(user.getCreatedAt().toString());
                    userDTO.setCreatedAt(createdAt.format(createdAtFormatter));
                    File file = fileRepository.getFileByType(user.getUserId(), "avatar");
                    if (file != null) {
                        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(file.getFileId().toString()).toUriString();
                        FileResponse fileResponse = new FileResponse(file.getFileType(), file.getFileName(), fileUrl);
                        userDTO.setAvatar(fileResponse);
                    }
                    return userDTO;
                }).toList();
        UserResponse userResponse = UserResponse.builder()
                    .totalItems(userPage.getTotalElements())
                    .totalPages(userPage.getTotalPages())
                    .currentPage(userPage.getNumber())
                    .users(users)
                    .build();
        response.setData(userResponse);
        return response;
    }

    @Override
    public ResponseEntityBase getAllDoctors(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Page<DoctorInfoProjection> doctorPage = doctorRepository.getDoctorsInfo(commonServiceImpl.pagingSort(page, size, sorts));
        List<DoctorInfoDTO> doctors = doctorPage.getContent().stream().map(doctorInfoProjection -> {
            DoctorInfoDTO doctorInfoDTO = DoctorMapper.DOCTOR_MAPPER.mapToDoctorInfo(doctorInfoProjection);
            String avatar = doctorInfoProjection.getFileType();
            String fileName = doctorInfoProjection.getFileName();
            if (avatar != null && fileName != null) {
                String fileUrl =
                        ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(doctorInfoProjection.getFileId().toString()).toUriString();
                FileResponse fileResponse = new FileResponse(avatar, fileName, fileUrl);
                doctorInfoDTO.setAvatar(fileResponse);
            }
            return doctorInfoDTO;
        }).toList();
        response.setData(doctors);
        return response;
    }

    @Override
    public ResponseEntityBase addService(ServiceRequest serviceRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Specialization specialization = specializationRepository.findById(serviceRequest.getSpecializationId()).orElseThrow(() ->
                new ResourceNotFoundException("Specialization ID" , serviceRequest.getSpecializationId().toString()));
        Services services = ServicesMapper.SERVICES_MAPPER.mapToServices(serviceRequest);
        services.setStatus(EServiceStatus.DRAFT);
        services.setSpecialization(specialization);
        services.setCreatedBy(currentUser.getUsername());
        servicesRepository.save(services);
        response.setData(MessageConstants.ADD_SERVICE_SUCCESS);
        return response;
    }

    @Override
    public ResponseEntityBase getServiceById(Long serviceId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Services service = servicesRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Id", serviceId.toString()));
        ServicesDTO servicesDTO =  ServicesDTO.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .servicePrice(service.getServicePrice())
                .description(service.getDescription())
                .status(service.getStatus().name())
                .specializationName(service.getSpecialization().getSpecializationName())
                .build();
        response.setData(servicesDTO);
        return response;
    }

    @Override
    public ResponseEntityBase updateService(ServiceRequest serviceRequest, Long serviceId) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Services service = servicesRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Id", serviceId.toString()));
        Specialization specialization = specializationRepository.findById(serviceRequest.getSpecializationId()).orElseThrow(() ->
                new ResourceNotFoundException("Specialization ID" , serviceRequest.getSpecializationId().toString()));
        if (service.getStatus().name().equals("INACTIVE") || service.getStatus().name().equals("DRAFT")) {
            service.setSpecialization(specialization);
            service.setServiceName(serviceRequest.getServiceName());
            service.setServicePrice(serviceRequest.getServicePrice());
            service.setDescription(serviceRequest.getDescription());
            service.setUpdatedBy(currentUser.getUsername());
            servicesRepository.save(service);
            response.setData(MessageConstants.UPDATE_SERVICE_SUCCESS);
        } else {
            throw new SystemException("Service that has been " +service.getStatus().name()+ " can't be updated.");
        }
        return response;
    }

    @Override
    public List<ServicesDTO> getServices() {
        return servicesRepository.findAll().stream()
                .map(services -> {
                    ServicesDTO servicesResponse = ServicesMapper.SERVICES_MAPPER.mapToServicesResponse(services);
                    servicesResponse.setSpecializationName(services.getSpecialization().getSpecializationName());
                    return servicesResponse;
                }).toList();
    }

    @Override
    public List<UserDTO> getUsers() {
        return userRepository.getUsers().stream()
                .map(user -> {
                    UserDTO userDTO = UserMapper.USER_MAPPER.mapToUserDTO(user);
                    userDTO.setUserAddress(commonServiceImpl.getAddress(user));
                    return userDTO;
                }).toList();
    }

    @Override
    public ResponseEntityBase getAllSpecializations() {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        List<SpecializationDTO> specializations = specializationRepository.findAll().stream()
                .map(SpecializationMapper.SPECIALIZATION_MAPPER::mapToSpecializationDTO).toList();
        response.setData(specializations);
        return response;
    }

    @Override
    public ResponseEntityBase getAllServices(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Page<Services> servicesPage = servicesRepository.findAll(commonServiceImpl.pagingSort(page, size, sorts));
        List<ServicesDTO> servicesResponses = servicesPage.getContent().stream()
                .map(services -> {
                    ServicesDTO servicesResponse = ServicesMapper.SERVICES_MAPPER.mapToServicesResponse(services);
                    servicesResponse.setSpecializationName(services.getSpecialization().getSpecializationName());
                    return servicesResponse;
                }).toList();
        ServicesResponse servicesResponse = ServicesResponse.builder()
                .totalItems(servicesPage.getTotalElements())
                .totalPages(servicesPage.getTotalPages())
                .currentPage(servicesPage.getNumber())
                .services(servicesResponses)
                .build();
        response.setData(servicesResponse);
        return response;
    }

    @Override
    public ResponseEntityBase getAllBookings(int page, int size, String[] sorts) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        Pageable pageable = commonServiceImpl.pagingSort(page, size, sorts);
        Page<BookingDetailsInfoProjection> bookingsPage = bookingRepository.getAllBookings(pageable);
        BookingResponse bookingResponse = BookingResponse.builder()
                .totalItems(bookingsPage.getTotalElements())
                .totalPages(bookingsPage.getTotalPages())
                .currentPage(bookingsPage.getNumber())
                .bookings(bookingsPage.getContent())
                .build();
        response.setData(bookingResponse);
        return response;
    }

    @Override
    @Transactional
    public ResponseEntityBase addTestPackage(TestPackageRequest testPackageRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        Services service = servicesRepository.findById(testPackageRequest.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service Id", testPackageRequest.getServiceId().toString()));
        String testPackageName = testPackageRequest.getTestPackageName();
        validateTestPackageName(testPackageName);
        TestPackage testPackage = new TestPackage();
        testPackage.setTestPackageName(testPackageName);
        testPackage.setTestPackagePrice(testPackageRequest.getTestPackagePrice());
        testPackage.setTestPreparationRequirements(testPackageRequest.getTestPreparationRequirements());
        testPackage.setTestDescription(testPackageRequest.getTestDescription());
        testPackage.setService(service);
        List<TestPackageAttribute> testPackageAttributes = createTestPackageAttributes(testPackageRequest, testPackage, currentUser);
        testPackage.setTestPackageAttributes(testPackageAttributes);
        testPackage.setStatus(ETestPackageStatus.DRAFT);
        testPackage.setCreatedBy(currentUser.getUsername());
        testPackageRepository.save(testPackage);
        response.setData(MessageConstants.ADD_TEST_PACKAGE_SUCCESS);
        return response;
    }

    @Override
    @Transactional
    public ResponseEntityBase updateTestPackage(Long testPackageId, TestPackageRequest testPackageRequest) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User currentUser = authService.getCurrentUser();
        TestPackage testPackage = testPackageRepository.findById(testPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("Test Package Id", testPackageId.toString()));
        Services service = servicesRepository.findById(testPackageRequest.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service Id", testPackageRequest.getServiceId().toString()));
        if (testPackage.getStatus().name().equals("INACTIVE") || testPackage.getStatus().name().equals("DRAFT")) {
            List<TestPackageAttribute> testPackageAttributes = testPackage.getTestPackageAttributes();
            if (Objects.nonNull(testPackageAttributes) && !testPackageAttributes.isEmpty()) {
                List<TestPackageAttribute> attributesToDelete = new ArrayList<>(testPackageAttributes);
                testPackageAttributes.forEach(testPackageAttribute -> {
                    normalRangeRepository.deleteNormalRangesByTestPackageAttributeId(testPackageAttribute.getTestPackageAttributeId());
                    testPackageAttribute.getTestPackages().remove(testPackage);
                });
                testPackage.getTestPackageAttributes().clear();
                testPackageRepository.save(testPackage);
                testPackageAttributeRepository.deleteAll(attributesToDelete);
            }
            String testPackageName = testPackageRequest.getTestPackageName();
            validateTestPackageName(testPackageName);
            testPackage.setTestPackageName(testPackageName);
            testPackage.setTestPackagePrice(testPackageRequest.getTestPackagePrice());
            testPackage.setTestPreparationRequirements(testPackageRequest.getTestPreparationRequirements());
            testPackage.setTestDescription(testPackageRequest.getTestDescription());
            testPackage.setService(service);
            List<TestPackageAttribute> savedTestPackageAttributes = createTestPackageAttributes(testPackageRequest, testPackage, currentUser);
            testPackage.setTestPackageAttributes(savedTestPackageAttributes);
            testPackage.setStatus(ETestPackageStatus.DRAFT);
            testPackage.setUpdatedBy(currentUser.getUsername());
            testPackageRepository.save(testPackage);
            response.setData(MessageConstants.UPDATE_TEST_PACKAGE_SUCCESS);
        } else {
            throw new SystemException("Test package that has been " +testPackage.getStatus().name()+ " can't be updated.");
        }
        return response;
    }

    @Override
    public ResponseEntityBase updateTestPackageStatus(Long testPackageId, String status) {
        ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
        User user = authService.getCurrentUser();
        TestPackage testPackage = testPackageRepository.findById(testPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("Test Package Id", testPackageId.toString()));
        ETestPackageStatus testPackageStatus = testPackage.getStatus();
        ETestPackageStatus newStatus = validateTestPackageStatus(status);
        boolean isValid = switch (testPackageStatus) {
            case DRAFT -> newStatus == ETestPackageStatus.ACTIVE;
            case ACTIVE -> newStatus == ETestPackageStatus.INACTIVE;
            case INACTIVE -> newStatus == ETestPackageStatus.ACTIVE || newStatus == ETestPackageStatus.DEPRECATED;
            default -> false;
        };
        if (!isValid) {
            throw new SystemException("The test package cannot change status from " + testPackageStatus + " to " + newStatus);
        }
        testPackage.setStatus(newStatus);
        testPackage.setUpdatedBy(user.getUsername());
        testPackageRepository.save(testPackage);
        response.setData(MessageConstants.UPDATE_TEST_PACKAGE_STATUS_SUCCESS);
        return response;
    }

    @Override
    public ByteArrayInputStream exportUsersToExcel(List<UserDTO> users) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Users");
            String[] headers = {"User ID", "User Code", "Full Name", "Email", "Date Of Birth",
                    "Gender", "Phone Number",  "Address", "Status", "Created At"};
            commonServiceImpl.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (UserDTO user : users) {
                Row currentRow = sheet.createRow(firstRow++);
                String fullName = Objects.isNull(user.getFirstName()) && Objects.isNull(user.getLastName()) ? "" : (user.getFirstName() + " " + user.getLastName());
                AddressResponse userAddress = user.getUserAddress();
                String address = "";
                if (userAddress.getAddressId() != null) {
                    address = userAddress.getSpecificAddress() + ", " + userAddress.getWardName() + ", " + userAddress.getDistrictName() + ", " + userAddress.getCityName();
                }
                String gender = user.getGender().equals("1") ? "Male" : "Female";
                String dateOfBirth = "";
                if (user.getDateOfBirth() != null) {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.ENGLISH);
                    LocalDateTime dateTime = LocalDateTime.parse(user.getDateOfBirth().replaceAll("[\\u202F\\u00A0]", " "), inputFormatter);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    dateOfBirth = dateTime.format(outputFormatter);
                }
                DateTimeFormatter createdAtFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                LocalDateTime createdAt = LocalDateTime.parse(user.getCreatedAt().toString());
                user.setCreatedAt(createdAt.format(createdAtFormatter));
                commonServiceImpl.createCell(workbook, currentRow, 0, user.getUserId());
                commonServiceImpl.createCell(workbook, currentRow, 1, user.getUserCode());
                commonServiceImpl.createCell(workbook, currentRow, 2, fullName);
                commonServiceImpl.createCell(workbook, currentRow, 3, user.getEmail());
                commonServiceImpl.createCell(workbook, currentRow, 4, dateOfBirth);
                commonServiceImpl.createCell(workbook, currentRow, 5, gender);
                commonServiceImpl.createCell(workbook, currentRow, 6, Objects.isNull(user.getPhoneNumber()) ? "" : user.getPhoneNumber());
                commonServiceImpl.createCell(workbook, currentRow, 7, address);
                commonServiceImpl.createCell(workbook, currentRow, 8, user.getStatus());
                commonServiceImpl.createCell(workbook, currentRow, 9, user.getCreatedAt());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new SystemException(MessageConstants.FAILED_EXPORT_DATA_EXCEL);
        }
    }

    @Override
    public ByteArrayInputStream exportServicesToExcel(List<ServicesDTO> services) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Services");
            String[] headers = {"Service Name", "Price", "Description", "Service Category", "Status"};
            commonServiceImpl.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (ServicesDTO service : services) {
                Row currentRow = sheet.createRow(firstRow++);
                commonServiceImpl.createCell(workbook, currentRow, 0, Objects.isNull(service.getServiceName()) ? " " : service.getServiceName());
                commonServiceImpl.createCell(workbook, currentRow, 1, service.getServicePrice());
                commonServiceImpl.createCell(workbook, currentRow, 2, Objects.isNull(service.getDescription()) ? " " : service.getDescription());
                commonServiceImpl.createCell(workbook, currentRow, 3, Objects.isNull(service.getSpecializationName()) ? " " : service.getSpecializationName());
                commonServiceImpl.createCell(workbook, currentRow, 4, Objects.isNull(service.getStatus()) ? " " : service.getStatus());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new SystemException(MessageConstants.FAILED_EXPORT_DATA_EXCEL);
        }
    }

    @Override
    public ByteArrayInputStream exportBookingsToExcel(List<BookingDetailsInfoProjection> bookings) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Bookings");
            String[] headers = {"Full Name", "Date Of Birth", "Gender", "Phone Number", "Address",
                    "Appointment Date", "Start Time", "End Time", "Specialization", "Describe Symptoms"};
            commonServiceImpl.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (BookingDetailsInfoProjection booking : bookings) {
                Row currentRow = sheet.createRow(firstRow++);
                String fullName = booking.getFirstName() + " " + booking.getLastName();
//                AddressResponse userAddress = booking.getUserAddress();
//                String address = userAddress.getSpecificAddress() + ", " + userAddress.getWardName() + ", " + userAddress.getDistrictName() + ", " + userAddress.getCityName();
                commonServiceImpl.createCell(workbook, currentRow, 0, (Objects.isNull(booking.getFirstName()) && Objects.isNull(booking.getLastName())) ? " " : fullName);
                commonServiceImpl.createCell(workbook, currentRow, 1, Objects.isNull(booking.getDateOfBirth()) ? " " : booking.getDateOfBirth());
//                commonServiceImpl.createCell(workbook, currentRow, 2, booking.getGender() == 1 ? "Male" : "Female");
                commonServiceImpl.createCell(workbook, currentRow, 3, Objects.isNull(booking.getPhoneNumber()) ? " " : booking.getPhoneNumber());
//                commonServiceImpl.createCell(workbook, currentRow, 4, Objects.isNull(booking.getUserAddress().getSpecificAddress()) ? " " : address);
//                commonServiceImpl.createCell(workbook, currentRow, 5, Objects.isNull(booking.getAppointmentDate()) ? " " : booking.getAppointmentDate());
                commonServiceImpl.createCell(workbook, currentRow, 6, Objects.isNull(booking.getStartTime()) ? " " : booking.getStartTime());
                commonServiceImpl.createCell(workbook, currentRow, 7, Objects.isNull(booking.getEndTime()) ? " " : booking.getEndTime());
//                commonServiceImpl.createCell(workbook, currentRow, 8, Objects.isNull(booking.getSpecialization()) ? " " : booking.getSpecialization());
                commonServiceImpl.createCell(workbook, currentRow, 9, Objects.isNull(booking.getDescribeSymptoms()) ? " " : booking.getDescribeSymptoms());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new SystemException(MessageConstants.FAILED_EXPORT_DATA_EXCEL);
        }
    }

    @Override
    public List<Services> importServicesFromExcel(InputStream inputStream) {
        try {
            List<Services> services = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("Services");
            if (sheet == null) {
                throw new ResourceNotFoundException("Sheet","Services");
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                Services service = new Services();
                List<Cell> cells = commonServiceImpl.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    String colName = serviceHeaderCellIndex(rows.get(0)).get(indexCell);
                    commonServiceImpl.checkBlankType(cells.get(indexCell), indexRow, colName);
                    switch (colName) {
                        case "Service Name" -> {
                            String serviceName = commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            List<String> servicesName = servicesRepository.findAll().stream().map(Services::getServiceName).toList();
                            servicesName.forEach(name -> {
                                if (serviceName.equals(name))
                                    throw new ResourceAlreadyExistException("Service",serviceName);
                            });
                            service.setServiceName(serviceName);
                        }
                        case "Price" ->
                                service.setServicePrice(commonServiceImpl.checkNumericType(cells.get(indexCell), indexRow, colName).getNumericCellValue());
                        case "Description" ->
                                service.setDescription(commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                        case "Service Category" -> {
                        }
                        case "Status" -> {
                            String serviceStatus = commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            if (Arrays.stream(EServiceStatus.values()).noneMatch(eServiceStatus -> eServiceStatus.name().equalsIgnoreCase(serviceStatus))) {
                                throw new SystemException(MessageConstants.INVALID_SERVICE_STATUS);
                            }
                            service.setStatus(EServiceStatus.valueOf(serviceStatus.toUpperCase()));
                        }
                        default -> {
                        }
                    }
                }
                services.add(service);
            }
            return services;
        } catch (IOException e) {
            throw new SystemException(MessageConstants.FAILED_IMPORT_DATA_EXCEL);
        }
    }

    @Override
    public ResponseEntityBase importBookingsFromExcel(InputStream inputStream) {
        try {
            ResponseEntityBase response = new ResponseEntityBase(HttpStatus.OK.value(), null, null);
            List<BookingExcelResponse> bookingExcelResponses = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("bookings");
            if (sheet == null) {
                throw new ResourceNotFoundException("Sheet","bookings");
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                BookingExcelResponse bookingExcelResponse = new BookingExcelResponse();
                bookingExcelResponse.setRowIndex(indexRow);
                List<Cell> cells = commonServiceImpl.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    Map<Integer, String> headerCellIndex = bookingHeaderCellIndex(rows.get(0));
                    String colName = headerCellIndex.get(indexCell);
                    commonServiceImpl.checkBlankType(cells.get(indexCell), indexRow, colName);
                    switch (colName) {
                        case "Full Name" ->
                                checkFullName(cells.get(indexCell), indexRow, colName, bookingExcelResponse);
                        case "Date Of Birth" ->
                                bookingExcelResponse.getBookingExcelDTO().setDateOfBirth(checkFormatDate(cells.get(indexCell), indexRow, colName));
                        case "Gender" ->
                                checkGender(cells.get(indexCell), indexRow, colName, bookingExcelResponse);
                        case "Phone Number" ->
                                bookingExcelResponse.getBookingExcelDTO().setPhoneNumber(commonServiceImpl.getPhoneNumberFromExcel(cells.get(indexCell), indexRow, colName));
                        case "Address" ->
                                bookingExcelResponse.getBookingExcelDTO().setAddress(commonServiceImpl.getAddressFromExcel(cells.get(indexCell), indexRow, colName));
                        case "Appointment Date" -> {
                            Date appointmentDate = checkFormatDate(cells.get(indexCell), indexRow, colName);
                            if (appointmentDate.before(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
                                throw new SystemException("Appointment date must be start from today.");
                            }
                            bookingExcelResponse.getBookingExcelDTO().setAppointmentDate(appointmentDate);
                        }
                        case "Specialization" ->
                                checkSpecificationName(cells.get(indexCell), indexRow, colName, bookingExcelResponse);
                        case "Start Time" ->
                                bookingExcelResponse.getBookingExcelDTO().setStartTime(commonServiceImpl.checkNumericType(cells.get(indexCell), indexRow, colName).getLocalDateTimeCellValue().toLocalTime());
                        case "End Time" ->
                                bookingExcelResponse.getBookingExcelDTO().setEndTime(commonServiceImpl.checkNumericType(cells.get(indexCell), indexRow, colName).getLocalDateTimeCellValue().toLocalTime());
                        case "Describe Symptoms" ->
                                bookingExcelResponse.getBookingExcelDTO().setDescribeSymptoms(commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                        default -> {
                        }
                    }
                }
                bookingExcelResponses.add(bookingExcelResponse);
            }
            BookingImportResponse bookingImportResponse = filterExcelBookingList(bookingExcelResponses);
            StringBuilder responseMessage = new StringBuilder();
            bookingRepository.saveAll(bookingImportResponse.getValidBookings());
            List<BookingExcelResponse> invalidBookings = bookingImportResponse.getInvalidBookings();
            if (invalidBookings.size() == 0) {
                responseMessage.append("Successfully imported all rows from excel file.");
            } else {
                StringBuilder rowsErrorMessage = new StringBuilder();
                for (int i=0; i<invalidBookings.size(); i++) {
                    rowsErrorMessage.append(invalidBookings.get(i).getRowIndex()+1).append((i != invalidBookings.size()-1) ? ",":"");
                }
                responseMessage.append("Successfully imported "+bookingImportResponse.getValidBookings().size()+" rows from excel file. " +
                        "Rows "+rowsErrorMessage+" were imported unsuccessfully.Please check your booking information again.");
            }
            response.setData(responseMessage.toString());
            return response;
        } catch (IOException e) {
            throw new SystemException(MessageConstants.FAILED_IMPORT_DATA_EXCEL);
        }
    }

    public String randomPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 8)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(randomIndex -> String.valueOf(chars.charAt(randomIndex)))
                .collect(Collectors.joining());
    }

    public Map<Integer, String> serviceHeaderCellIndex (Row row) {
        Map <Integer, String> headerCellIndex = new HashMap<>();
        List<Cell> cellsHeader = commonServiceImpl.getAllCells(row);
        for (int i = 0; i < cellsHeader.size(); i++) {
            switch (cellsHeader.get(i).getStringCellValue()) {
                case "Service Name" -> headerCellIndex.put(i, "Service Name");
                case "Price" -> headerCellIndex.put(i, "Price");
                case "Description" -> headerCellIndex.put(i, "Description");
                case "Service Category" -> headerCellIndex.put(i, "Service Category");
                case "Status" -> headerCellIndex.put(i, "Status");
                default -> {
                }
            }
        }
        return headerCellIndex;
    }

    public Map<Integer, String> bookingHeaderCellIndex (Row row) {
        Map <Integer, String> headerCellIndex = new HashMap<>();
        List<Cell> cellsHeader = commonServiceImpl.getAllCells(row);
        for (int i = 0; i < cellsHeader.size(); i++) {
            switch (cellsHeader.get(i).getStringCellValue()) {
                case "Full Name" -> headerCellIndex.put(i, "Full Name");
                case "Date Of Birth" -> headerCellIndex.put(i, "Date Of Birth");
                case "Gender" -> headerCellIndex.put(i, "Gender");
                case "Phone Number" -> headerCellIndex.put(i, "Phone Number");
                case "Address" -> headerCellIndex.put(i, "Address");
                case "Specialization" -> headerCellIndex.put(i, "Specialization");
                case "Appointment Date" -> headerCellIndex.put(i, "Appointment Date");
                case "Start Time" -> headerCellIndex.put(i, "Start Time");
                case "End Time" -> headerCellIndex.put(i, "End Time");
                case "Describe Symptoms" -> headerCellIndex.put(i, "Describe Symptoms");
                default -> {
                }
            }
        }
        return headerCellIndex;
    }

    private Date checkFormatDate (Cell cell, int indexRow, String colName) {
        Date date = commonServiceImpl.checkDateType(cell, indexRow, colName).getDateCellValue();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (!dateFormat.format(date).matches("^\\d{4}-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$")) {
            throw new SystemException("Import failed. "+ colName +" in row " + (indexRow + 1) + " must be yyyy-MM-dd format.");
        }
        return date;
    }

    private void checkFullName (Cell cell, int indexRow, String colName, BookingExcelResponse bookingExcelResponse) {
        String fullName = commonServiceImpl.checkStringType(cell, indexRow, colName).getStringCellValue();
        bookingExcelResponse.getBookingExcelDTO().setFirstName(fullName.split(" ")[0]);
        bookingExcelResponse.getBookingExcelDTO().setLastName(fullName.substring(fullName.split(" ")[0].length() + 1));
    }

    private void checkGender (Cell cell, int indexRow, String colName, BookingExcelResponse bookingExcelResponse) {
        String gender = commonServiceImpl.checkStringType(cell, indexRow, colName).getStringCellValue();
        if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")) {
            throw new SystemException("Import failed. "+ colName +" in row " + (indexRow + 1) + " must be 'Male' or 'Female'.");
        }
        bookingExcelResponse.getBookingExcelDTO().setGender(gender.equalsIgnoreCase("Male") ? 1 : 0);
    }

    private void checkSpecificationName (Cell cell, int indexRow, String colName, BookingExcelResponse bookingExcelResponse) {
        String specializationName = commonServiceImpl.checkStringType(cell, indexRow, colName).getStringCellValue();
        Specialization specialization = specializationRepository.getSpecializationBySpecializationName(specializationName);
        if (Objects.isNull(specialization)) {
            throw new SystemException("Import failed. Specialization named " + specializationName + " is not exist.");
        }
        bookingExcelResponse.getBookingExcelDTO().setSpecializationName(specializationName);
    }

    public BookingImportResponse filterExcelBookingList (List<BookingExcelResponse> bookingExcelResponses) {
        BookingImportResponse bookingImportResponse = new BookingImportResponse();
        List<BookingExcelResponse> invalidBookings = new ArrayList<>();
        for (BookingExcelResponse bookingExcelResponse : bookingExcelResponses) {
            Specialization specialization = specializationRepository.getSpecializationBySpecializationName(bookingExcelResponse.getBookingExcelDTO().getSpecializationName());
            LocalDate appointmentDate = bookingExcelResponse.getBookingExcelDTO().getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            User doctor = userRepository.getUserFromExcel(specialization.getSpecializationId(), appointmentDate.getDayOfWeek(),
                    bookingExcelResponse.getBookingExcelDTO().getStartTime(), bookingExcelResponse.getBookingExcelDTO().getEndTime());
            if (Objects.isNull(doctor)) {
                invalidBookings.add(bookingExcelResponse);
                continue;
            }
//            bookingRepository.getBookingsByDoctor(doctor.getUserId()).forEach(booking -> {
//                LocalTime startTime = booking.getWorkSchedule().getStartTime();
//                LocalTime endTime = booking.getWorkSchedule().getEndTime();
//                if (bookingExcelResponse.getBookingExcelDTO().getAppointmentDate().equals(booking.getAppointmentDate())
//                        && bookingExcelResponse.getBookingExcelDTO().getStartTime().equals(startTime)
//                        && bookingExcelResponse.getBookingExcelDTO().getEndTime().equals(endTime)) {
//                    invalidBookings.add(bookingExcelResponse);
//                }
//            });
        }
        bookingImportResponse.setInvalidBookings(invalidBookings);
        if (invalidBookings.size() > 0) {
            invalidBookings.stream()
                    .map(BookingExcelResponse::getBookingExcelDTO)
                    .forEach(bookingExcelDTO -> bookingExcelResponses.removeIf(response -> response.getBookingExcelDTO().equals(bookingExcelDTO)));
        }
        bookingImportResponse.setValidBookings(convertToBookingList(bookingExcelResponses));
        return bookingImportResponse;
    }

    public List<Booking> convertToBookingList (List<BookingExcelResponse> bookingExcelResponses) {
        List<Booking> bookings = bookingExcelResponses.stream()
                .map(bookingExcelResponse -> {
                    Booking booking = BookingMapper.BOOKING_MAPPER.mapExcelToBooking(bookingExcelResponse.getBookingExcelDTO());
                    LocalDate appointmentDate = bookingExcelResponse.getBookingExcelDTO().getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//                    WorkSchedule workSchedule = workScheduleRepository.getWorkScheduleByTime(
//                            bookingExcelResponse.getBookingExcelDTO().getSpecializationName(), appointmentDate.getDayOfWeek(),
//                            bookingExcelResponse.getBookingExcelDTO().getStartTime(), bookingExcelResponse.getBookingExcelDTO().getEndTime());
//                    booking.setWorkSchedule(workSchedule);
                    booking.setStatus(EBookingStatus.PENDING);
                    return booking;
                }).toList();
        setBookingCode(bookings);
        return bookings;
    }

    public void setBookingCode (List<Booking> bookings) {
        Long maxBookingCode;
        if (bookingRepository.findAll().size() == 0) {
            maxBookingCode = 1L;
            for (Booking booking : bookings) {
                booking.setBookingCode("BC" + (maxBookingCode++));
            }
        } else {
            maxBookingCode = Collections.max(bookingRepository.findAll()
                    .stream()
                    .map(booking -> Long.parseLong(booking.getBookingCode().substring(2)))
                    .toList());
            for (Booking booking : bookings) {
                booking.setBookingCode("BC" + (++maxBookingCode));
            }
        }
    }

    private ETestPackageStatus validateTestPackageStatus(String status) {
        String enumValue = "";
        if (status.equalsIgnoreCase("ACTIVE")) {
            enumValue = "ACTIVE";
        } else if (status.equalsIgnoreCase("INACTIVE")) {
            enumValue = "INACTIVE";
        } else if (status.equalsIgnoreCase("DRAFT")) {
            enumValue = "DRAFT";
        } else if (status.equalsIgnoreCase("DEPRECATED")) {
            enumValue = "DEPRECATED";
        } else {
            throw new ResourceNotFoundException("Test Package Status", status);
        }
        return ETestPackageStatus.valueOf(enumValue);
    }

    public void validateNormalRange(NormalRange normalRange, String testPackageAttributeUnit) {
        ENormalRangeType normalRangeType = normalRange.getNormalRangeType();
        switch (normalRangeType) {
            case RANGE -> {
                requireNonNull(normalRange.getMinValue(), "Min value is required for RANGE.");
                requireNonNull(normalRange.getMaxValue(), "Max value is required for RANGE.");
                if (testPackageAttributeUnit == null || testPackageAttributeUnit.equals(""))
                    throw new SystemException("Unit is required for RANGE.");
            }
            case LESS_THAN, LESS_THAN_EQUAL-> {
                requireNonNull(normalRange.getMaxValue(), normalRangeType + " requires max value.");
                if (testPackageAttributeUnit == null || testPackageAttributeUnit.equals(""))
                    throw new SystemException(normalRangeType + " requires unit value.");
            }
            case GREATER_THAN, GREATER_THAN_EQUAL-> {
                requireNonNull(normalRange.getMinValue(), normalRangeType + " requires min value.");
                if (testPackageAttributeUnit == null || testPackageAttributeUnit.equals(""))
                    throw new SystemException(normalRangeType + " requires unit value.");
            }
            case EQUAL -> {
                requireNonNull(normalRange.getEqualValue(), normalRangeType + " requires equal value.");
                if (testPackageAttributeUnit == null || testPackageAttributeUnit.equals(""))
                    throw new SystemException(normalRangeType + " requires unit value.");
            }
            case QUALITATIVE -> {
                requireNonNull(normalRange.getExpectedValue(), "Expected value is required for QUALITATIVE.");
            }
            case SEMI_QUALITATIVE -> {
                requireNonNull(normalRange.getExpectedValue(), "Expected value is required for SEMI QUALITATIVE");
                requireNonNull(normalRange.getNormalText(), "Normal text is required for SEMI QUALITATIVE.");
            }
            case TEXT -> {
                requireNonNull(normalRange.getNormalText(), "Normal text is required for TEXT.");
            }
            default -> {
                throw new ResourceNotFoundException("Normal range type", normalRangeType.name());
            }
        }
    }

    private void requireNonNull(Object value, String message) {
        if (value == null) throw new SystemException(message);
    }

    private List<TestPackageAttribute> createTestPackageAttributes(TestPackageRequest testPackageRequest, TestPackage testPackage, User currentUser) {
        List<TestPackageAttribute> testPackageAttributes = new ArrayList<>();
        testPackageRequest.getTestPackageAttributes().forEach(testPackageAttributeDTO -> {
            TestPackageAttribute testPackageAttribute = new TestPackageAttribute();
            String testPackageAttributeName = testPackageAttributeDTO.getName();
            List<String> testPackageAttributeNames = testPackageAttributeRepository.findAll().stream().map(TestPackageAttribute::getName).toList();
            if (testPackageAttributeNames.stream().anyMatch(s -> s.equalsIgnoreCase(testPackageAttributeName))) {
                throw new ResourceAlreadyExistException("Test package attribute name", testPackageAttributeName);
            }
            testPackageAttribute.setName(testPackageAttributeName);
            String testPackageAttributeUnit = testPackageAttributeDTO.getUnit();
            Map<String, String> attributeMetaData = testPackageAttributeDTO.getAttributeMetadata();
            if (attributeMetaData.containsKey("name") || attributeMetaData.containsKey("unit")) {
                throw new SystemException(MessageConstants.ERROR_ADD_NAME_OR_UNIT_ATTRIBUTE);
            }
            testPackageAttribute.setAttributeMetadata(attributeMetaData);
            if (testPackageAttributeDTO.getNormalRanges() == null || testPackageAttributeDTO.getNormalRanges().size() == 0) {
                throw new SystemException(MessageConstants.ERROR_NORMAL_RANGE_BLANK);
            }
            List<NormalRange> normalRanges = testPackageAttributeDTO.getNormalRanges()
                    .stream()
                    .map(normalRangeDTO -> {
                        List<String> normalRangeTypes = Arrays.stream(ENormalRangeType.values())
                                .map(Enum::name)
                                .toList();
                        if (normalRangeTypes.stream().noneMatch(s -> s.equalsIgnoreCase(normalRangeDTO.getNormalRangeType()))) {
                            throw new ResourceNotFoundException("Normal range type", normalRangeDTO.getNormalRangeType());
                        }
                        List<String> genderTypes = Arrays.stream(EGender.values())
                                .map(Enum::name)
                                .toList();
                        if (genderTypes.stream().noneMatch(s -> s.equalsIgnoreCase(normalRangeDTO.getGender()))) {
                            throw new ResourceNotFoundException("Gender type", normalRangeDTO.getGender());
                        }
                        normalRangeDTO.setNormalRangeType(normalRangeDTO.getNormalRangeType().toUpperCase());
                        NormalRange normalRange = NormalRangeMapper.NORMAL_RANGE_MAPPER.mapToNormalRange(normalRangeDTO);
                        validateNormalRange(normalRange, testPackageAttributeUnit);
                        normalRange.setTestPackageAttribute(testPackageAttribute);
                        normalRange.setCreatedBy(currentUser.getUsername());
                        return normalRange;
                    }).toList();
            testPackageAttribute.setUnit(testPackageAttributeUnit);
            testPackageAttribute.setNormalRanges(normalRanges);
            if (testPackageAttribute.getTestPackages() == null) {
                testPackageAttribute.setTestPackages(new ArrayList<>());
            }
            testPackageAttribute.getTestPackages().add(testPackage);
            testPackageAttribute.setCreatedBy(currentUser.getUsername());
            testPackageAttributes.add(testPackageAttribute);
        });
        return testPackageAttributes;
    }

    private void validateTestPackageName(String testPackageName) {
        List<String> testPackageNames = testPackageRepository.findAll().stream().map(TestPackage::getTestPackageName).toList();
        if (testPackageNames.stream().anyMatch(s -> s.equalsIgnoreCase(testPackageName))) {
            throw new ResourceAlreadyExistException("Test package name", testPackageName);
        }
    }

}
