package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EBookingStatus;
import khanhnq.project.clinicbookingmanagementsystem.exception.*;
import khanhnq.project.clinicbookingmanagementsystem.mapper.*;
import khanhnq.project.clinicbookingmanagementsystem.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EServiceStatus;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final SpecializationRepository specializationRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServicesRepository servicesRepository;
    private final BookingRepository bookingRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final CommonServiceImpl commonServiceImpl;
    private final Workbook workbook = new XSSFWorkbook();
    private final JavaMailSender mailSender;
    private PasswordEncoder passwordEncoder;

    @Override
    public String resetPassword(String email) throws MessagingException {
        checkAccess();
        return resetPasswordEmail(email);
    }

    @Override
    public UserResponse getAllUsers(int page, int size, String[] sorts) {
        checkAccess();
        Page<User> userPage = userRepository.getAllUsers(commonServiceImpl.pagingSort(page, size, sorts));
        List<UserDTO> users = userPage.getContent().stream()
                .map(user -> {
                    UserDTO userDTO = UserMapper.USER_MAPPER.mapToUserDTO(user);
                    userDTO.setUserAddress(commonServiceImpl.getAddress(user));
                    return userDTO;
                }).toList();
        return UserResponse.builder()
                .totalItems(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .users(users)
                .build();
    }

    @Override
    public DoctorResponse getAllDoctors(int page, int size, String[] sorts) {
        checkAccess();
        Page<User> doctorPage = userRepository.getAllDoctors(commonServiceImpl.pagingSort(page, size, sorts));
        List<DoctorDTO> doctors = doctorPage.getContent().stream()
                .map(user -> {
                    DoctorDTO doctorDTO = UserMapper.USER_MAPPER.mapToDoctorResponse(user);
                    if (user.getSpecialization() != null)
                        doctorDTO.setSpecializationName(user.specializationName());
                    doctorDTO.setDoctorAddress(commonServiceImpl.getAddress(user));
                    doctorDTO.setFiles(commonServiceImpl.getAllFiles(user.getUserId()));
                    return doctorDTO;
                }).toList();
        return DoctorResponse.builder()
                .totalItems(doctorPage.getTotalElements())
                .totalPages(doctorPage.getTotalPages())
                .currentPage(doctorPage.getNumber())
                .doctors(doctors)
                .build();
    }

    @Override
    public String addServiceCategory(ServiceCategoryRequest serviceCategoryRequest) {
        checkAccess();
        Specialization specialization = specializationRepository.findById(serviceCategoryRequest.getSpecializationId()).orElse(null);
        ServiceCategory serviceCategory = ServiceCategoryMapper.SERVICE_CATEGORY_MAPPER.mapToServiceCategory(serviceCategoryRequest);
        serviceCategory.setSpecialization(specialization);
        serviceCategoryRepository.save(serviceCategory);
        return MessageConstants.ADD_SERVICE_CATEGORY_SUCCESS;
    }

    @Override
    public String addService(ServiceRequest serviceRequest) {
        checkAccess();
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceRequest.getServiceCategoryId()).orElse(null);
        Services services = ServicesMapper.SERVICES_MAPPER.mapToServices(serviceRequest);
        services.setStatus(EServiceStatus.ACTIVE);
        services.setServiceCategory(serviceCategory);
        servicesRepository.save(services);
        return MessageConstants.ADD_SERVICE_SUCCESS;
    }

    @Override
    public ServicesDTO getServiceById(Long serviceId) {
        Services services = servicesRepository.findById(serviceId).orElse(null);
        return ServicesDTO.builder()
                .serviceId(Objects.requireNonNull(services).getServiceId())
                .serviceName(services.getServiceName())
                .price(services.getPrice())
                .description(services.getDescription())
                .status(services.getStatus().name())
                .serviceCategoryName(services.serviceCategoryName())
                .build();
    }

    @Override
    public String updateService(ServiceRequest serviceRequest, Long serviceId) {
        checkAccess();
        Services service = servicesRepository.findById(serviceId).orElse(null);
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceRequest.getServiceCategoryId()).orElse(null);
        Objects.requireNonNull(service).setServiceCategory(serviceCategory);
        service.setServiceName(serviceRequest.getServiceName());
        service.setPrice(serviceRequest.getPrice());
        service.setDescription(serviceRequest.getDescription());
        servicesRepository.save(service);
        return MessageConstants.UPDATE_SERVICE_SUCCESS;
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
    public List<BookingDTO> getBookings() {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> Objects.isNull(booking.getUser())).toList();
        return bookings.stream()
                .map(booking -> {
                    BookingDTO bookingDTO = BookingMapper.BOOKING_MAPPER.mapToBookingDTO(booking);
                    bookingDTO.setUserAddress(commonServiceImpl.getAddress(booking));
                    bookingDTO.setStartTime(booking.getWorkSchedule().getStartTime());
                    bookingDTO.setEndTime(booking.getWorkSchedule().getEndTime());
                    return bookingDTO;
                }).toList();
    }

    @Override
    public List<SpecializationDTO> getAllSpecializations() {
        return specializationRepository.findAll().stream()
                .map(SpecializationMapper.SPECIALIZATION_MAPPER::mapToSpecializationDTO).toList();
    }

    @Override
    public List<ServiceCategoryDTO> getServiceCategories() {
        return serviceCategoryRepository.findAll().stream()
                .map(serviceCategory -> ServiceCategoryDTO.builder()
                        .serviceCategoryName(serviceCategory.getServiceCategoryName())
                        .description(serviceCategory.getDescription())
                        .specializationName(serviceCategory.getSpecialization().getSpecializationName())
                        .build())
                .toList();
    }

    @Override
    public List<ServicesDTO> getServices() {
        return servicesRepository.findAll().stream()
                .map(services -> {
                    ServicesDTO servicesResponse = ServicesMapper.SERVICES_MAPPER.mapToServicesResponse(services);
                    servicesResponse.setServiceCategoryName(services.serviceCategoryName());
                    return servicesResponse;
                }).toList();
    }

    @Override
    public List<ServiceCategoryDTO> getServiceCategoriesBySpecialization(Long specializationId) {
        return serviceCategoryRepository.getServiceCategoriesBySpecializationId(specializationId).stream()
                .map(serviceCategory -> ServiceCategoryDTO.builder()
                        .serviceCategoryId(serviceCategory.getServiceCategoryId())
                        .serviceCategoryName(serviceCategory.getServiceCategoryName())
                        .specializationId(specializationId)
                        .build())
                .toList();
    }

    @Override
    public ServiceCategoryResponse getAllServiceCategories(int page, int size, String[] sorts) {
        Page<ServiceCategory> serviceCategoryPage = serviceCategoryRepository.findAll(commonServiceImpl.pagingSort(page, size, sorts));
        List<ServiceCategoryDTO> serviceCategories = serviceCategoryPage.getContent().stream()
                .map(serviceCategory -> ServiceCategoryDTO.builder()
                        .serviceCategoryId(serviceCategory.getServiceCategoryId())
                        .serviceCategoryName(serviceCategory.getServiceCategoryName())
                        .description(serviceCategory.getDescription())
                        .specializationId(serviceCategory.getSpecialization().getSpecializationId())
                        .specializationName(serviceCategory.getSpecialization().getSpecializationName())
                        .build())
                .toList();
        return ServiceCategoryResponse.builder()
                .totalItems(serviceCategoryPage.getTotalElements())
                .totalPages(serviceCategoryPage.getTotalPages())
                .currentPage(serviceCategoryPage.getNumber())
                .serviceCategories(serviceCategories)
                .build();
    }

    @Override
    public ServicesResponse getAllServices(int page, int size, String[] sorts) {
        Page<Services> servicesPage = servicesRepository.findAll(commonServiceImpl.pagingSort(page, size, sorts));
        List<ServicesDTO> servicesResponses = servicesPage.getContent().stream()
                .map(services -> {
                    ServicesDTO servicesResponse = ServicesMapper.SERVICES_MAPPER.mapToServicesResponse(services);
                    servicesResponse.setServiceCategoryName(services.serviceCategoryName());
                    return servicesResponse;
                }).toList();
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
        checkAccess();
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId).orElse(null);
        Objects.requireNonNull(serviceCategory).setSpecialization(specializationRepository.findById(serviceCategoryRequest.getSpecializationId()).orElse(null));
        serviceCategory.setServiceCategoryName(serviceCategoryRequest.getServiceCategoryName());
        serviceCategory.setDescription(serviceCategoryRequest.getDescription());
        serviceCategoryRepository.save(serviceCategory);
        return MessageConstants.UPDATE_SERVICE_CATEGORY_SUCCESS;
    }

    @Override
    public ByteArrayInputStream exportUsersToExcel(List<UserDTO> users) {
        try {
            checkAccess();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Users");
            String[] headers = {"User Code", "Email", "Full Name", "Date Of Birth",
                    "Gender", "Phone Number", "Address", "Status"};
            commonServiceImpl.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (UserDTO user : users) {
                Row currentRow = sheet.createRow(firstRow++);
                String fullName = user.getFirstName() + " " + user.getLastName();
                AddressResponse userAddress = user.getUserAddress();
                String address = userAddress.getSpecificAddress() + ", " + userAddress.getWardName() + ", " + userAddress.getDistrictName() + ", " + userAddress.getCityName();
                commonServiceImpl.createCell(workbook, currentRow, 0, user.getUserCode());
                commonServiceImpl.createCell(workbook, currentRow, 1, user.getEmail());
                commonServiceImpl.createCell(workbook, currentRow, 2, Objects.isNull(user.getFirstName()) && Objects.isNull(user.getLastName()) ? " " : fullName);
                commonServiceImpl.createCell(workbook, currentRow, 3, Objects.isNull(user.getDateOfBirth()) ? " " : user.getDateOfBirth());
                commonServiceImpl.createCell(workbook, currentRow, 4, user.getGender() == 1 ? "Male" : "Female");
                commonServiceImpl.createCell(workbook, currentRow, 5, Objects.isNull(user.getPhoneNumber()) ? " " : user.getPhoneNumber());
                commonServiceImpl.createCell(workbook, currentRow, 6, Objects.isNull(user.getUserAddress().getSpecificAddress()) ? " " : address);
                commonServiceImpl.createCell(workbook, currentRow, 7, user.getStatus());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException(MessageConstants.FAILED_EXPORT_DATA_EXCEL);
        }
    }

    @Override
    public ByteArrayInputStream exportServiceCategoriesToExcel(List<ServiceCategoryDTO> serviceCategories) {
        try {
            checkAccess();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Service Categories");
            String[] headers = {"Service Category Name", "Description", "Specialization"};
            commonServiceImpl.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (ServiceCategoryDTO serviceCategory : serviceCategories) {
                Row currentRow = sheet.createRow(firstRow++);
                commonServiceImpl.createCell(workbook, currentRow, 0, serviceCategory.getServiceCategoryName());
                commonServiceImpl.createCell(workbook, currentRow, 1, serviceCategory.getDescription());
                commonServiceImpl.createCell(workbook, currentRow, 2, serviceCategory.getSpecializationName());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException(MessageConstants.FAILED_EXPORT_DATA_EXCEL);
        }
    }

    @Override
    public ByteArrayInputStream exportServicesToExcel(List<ServicesDTO> services) {
        try {
//            checkAccess();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Services");
            String[] headers = {"Service Name", "Price", "Description", "Service Category", "Status"};
            commonServiceImpl.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (ServicesDTO service : services) {
                Row currentRow = sheet.createRow(firstRow++);
                commonServiceImpl.createCell(workbook, currentRow, 0, Objects.isNull(service.getServiceName()) ? " " : service.getServiceName());
                commonServiceImpl.createCell(workbook, currentRow, 1, service.getPrice());
                commonServiceImpl.createCell(workbook, currentRow, 2, Objects.isNull(service.getDescription()) ? " " : service.getDescription());
                commonServiceImpl.createCell(workbook, currentRow, 3, Objects.isNull(service.getServiceCategoryName()) ? " " : service.getServiceCategoryName());
                commonServiceImpl.createCell(workbook, currentRow, 4, Objects.isNull(service.getStatus()) ? " " : service.getStatus());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException(MessageConstants.FAILED_EXPORT_DATA_EXCEL);
        }
    }

    @Override
    public ByteArrayInputStream exportBookingsToExcel(List<BookingDTO> bookings) {
        try {
            checkAccess();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Bookings");
            String[] headers = {"Full Name", "Date Of Birth", "Gender", "Phone Number", "Address",
                    "Appointment Date", "Start Time", "End Time", "Specialization", "Describe Symptoms"};
            commonServiceImpl.createHeader(workbook, sheet, headers);
            int firstRow = 1;
            for (BookingDTO booking : bookings) {
                Row currentRow = sheet.createRow(firstRow++);
                String fullName = booking.getFirstName() + " " + booking.getLastName();
                AddressResponse userAddress = booking.getUserAddress();
                String address = userAddress.getSpecificAddress() + ", " + userAddress.getWardName() + ", " + userAddress.getDistrictName() + ", " + userAddress.getCityName();
                commonServiceImpl.createCell(workbook, currentRow, 0, (Objects.isNull(booking.getFirstName()) && Objects.isNull(booking.getLastName())) ? " " : fullName);
                commonServiceImpl.createCell(workbook, currentRow, 1, Objects.isNull(booking.getDateOfBirth()) ? " " : booking.getDateOfBirth());
                commonServiceImpl.createCell(workbook, currentRow, 2, booking.getGender() == 1 ? "Male" : "Female");
                commonServiceImpl.createCell(workbook, currentRow, 3, Objects.isNull(booking.getPhoneNumber()) ? " " : booking.getPhoneNumber());
                commonServiceImpl.createCell(workbook, currentRow, 4, Objects.isNull(booking.getUserAddress().getSpecificAddress()) ? " " : address);
                commonServiceImpl.createCell(workbook, currentRow, 5, Objects.isNull(booking.getAppointmentDate()) ? " " : booking.getAppointmentDate());
                commonServiceImpl.createCell(workbook, currentRow, 6, Objects.isNull(booking.getStartTime()) ? " " : booking.getStartTime());
                commonServiceImpl.createCell(workbook, currentRow, 7, Objects.isNull(booking.getEndTime()) ? " " : booking.getEndTime());
                commonServiceImpl.createCell(workbook, currentRow, 8, Objects.isNull(booking.getSpecialization()) ? " " : booking.getSpecialization());
                commonServiceImpl.createCell(workbook, currentRow, 9, Objects.isNull(booking.getDescribeSymptoms()) ? " " : booking.getDescribeSymptoms());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException(MessageConstants.FAILED_EXPORT_DATA_EXCEL);
        }
    }

    @Override
    public List<ServiceCategory> importServiceCategoriesFromExcel(InputStream inputStream) {
        try {
            checkAccess();
            List<ServiceCategory> serviceCategories = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("Service Categories");
            if (sheet == null) {
                throw new ResourceNotFoundException("Sheet","Service Categories");
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                ServiceCategory serviceCategory = new ServiceCategory();
                List<Cell> cells = commonServiceImpl.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    String colName = serviceCategoryHeaderCellIndex(rows.get(0)).get(indexCell);
                    commonServiceImpl.checkBlankType(cells.get(indexCell), indexRow, colName);
                    switch (colName) {
                        case "Service Category Name" -> {
                            String serviceCategoryName = commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            List<String> serviceCategoriesName = serviceCategoryRepository.findAll().stream().map(ServiceCategory::getServiceCategoryName).toList();
                            if (serviceCategoriesName.stream().anyMatch(s -> s.equalsIgnoreCase(serviceCategoryName))) {
                                throw new ResourceAlreadyExistException("Service category",serviceCategoryName);
                            }
                            serviceCategory.setServiceCategoryName(serviceCategoryName);
                        }
                        case "Description" ->
                                serviceCategory.setDescription(commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                        case "Specialization" -> {
                            String specializationName = commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            Specialization specialization = specializationRepository.getSpecializationBySpecializationName(specializationName);
                            if (Objects.isNull(specialization)) {
                                throw new BusinessException("Import failed. Specialization named '"+specializationName+"' in row " + indexRow + " is not exist.");
                            }
                            serviceCategory.setSpecialization(specialization);
                        }
                    }
                }
                serviceCategories.add(serviceCategory);
            }
            return serviceCategories;
        } catch (IOException e) {
            throw new BusinessException(MessageConstants.FAILED_IMPORT_DATA_EXCEL);
        }
    }

    @Override
    public List<Services> importServicesFromExcel(InputStream inputStream) {
        try {
            checkAccess();
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
                                service.setPrice(commonServiceImpl.checkNumericType(cells.get(indexCell), indexRow, colName).getNumericCellValue());
                        case "Description" ->
                                service.setDescription(commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                        case "Service Category" -> {
                            ServiceCategory serviceCategory =
                                    serviceCategoryRepository.getServiceCategoryByServiceCategoryName(commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                            if (Objects.isNull(serviceCategory)) {
                                throw new BusinessException("Import failed. Service category name in row " + indexRow + " is not exist.");
                            }
                            service.setServiceCategory(serviceCategory);
                        }
                        case "Status" -> {
                            String serviceStatus = commonServiceImpl.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            if (Arrays.stream(EServiceStatus.values()).noneMatch(eServiceStatus -> eServiceStatus.name().equalsIgnoreCase(serviceStatus))) {
                                throw new BusinessException(MessageConstants.INVALID_SERVICE_STATUS);
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
            throw new BusinessException(MessageConstants.FAILED_IMPORT_DATA_EXCEL);
        }
    }

    @Override
    public String importBookingsFromExcel(InputStream inputStream) {
        try {
            checkAccess();
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
                                throw new BusinessException("Appointment date must be start from today.");
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
            return responseMessage.toString();
        } catch (IOException e) {
            throw new BusinessException(MessageConstants.FAILED_IMPORT_DATA_EXCEL);
        }
    }

    @Override
    public BookingResponse getAllBookings(int page, int size, String[] sorts) {
        checkAccess();
        Page<Booking> bookingPage = bookingRepository.getBookingsWithNullUser(commonServiceImpl.pagingSort(page, size, sorts));
        return commonServiceImpl.getAllBookings(bookingPage);
    }

    public String resetPasswordEmail(String email) throws MessagingException {
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
            return MessageConstants.RESET_PASSWORD_SUCCESS;
        } else {
            throw new ResourceNotFoundException("Email", email);
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

    public void checkAccess() {
        User currentUser = authService.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            throw new UnauthorizedException(MessageConstants.UNAUTHORIZED_ACCESS);
        }
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new ForbiddenException(MessageConstants.FORBIDDEN_ACCESS);
        }
    }

    public Map<Integer, String> serviceCategoryHeaderCellIndex (Row row) {
        Map <Integer, String> headerCellIndex = new HashMap<>();
        List<Cell> cellsHeader = commonServiceImpl.getAllCells(row);
        for (int i = 0; i < cellsHeader.size(); i++) {
            switch (cellsHeader.get(i).getStringCellValue()) {
                case "Service Category Name" -> headerCellIndex.put(i, "Service Category Name");
                case "Description" -> headerCellIndex.put(i, "Description");
                case "Specialization" -> headerCellIndex.put(i, "Specialization");
                default -> {
                }
            }
        }
        return headerCellIndex;
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
            throw new BusinessException("Import failed. "+ colName +" in row " + (indexRow + 1) + " must be yyyy-MM-dd format.");
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
            throw new BusinessException("Import failed. "+ colName +" in row " + (indexRow + 1) + " must be 'Male' or 'Female'.");
        }
        bookingExcelResponse.getBookingExcelDTO().setGender(gender.equalsIgnoreCase("Male") ? 1 : 0);
    }

    private void checkSpecificationName (Cell cell, int indexRow, String colName, BookingExcelResponse bookingExcelResponse) {
        String specializationName = commonServiceImpl.checkStringType(cell, indexRow, colName).getStringCellValue();
        Specialization specialization = specializationRepository.getSpecializationBySpecializationName(specializationName);
        if (Objects.isNull(specialization)) {
            throw new BusinessException("Import failed. Specialization named " + specializationName + " is not exist.");
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
            bookingRepository.getBookingsByDoctor(doctor.getUserId()).forEach(booking -> {
                LocalTime startTime = booking.getWorkSchedule().getStartTime();
                LocalTime endTime = booking.getWorkSchedule().getEndTime();
                if (bookingExcelResponse.getBookingExcelDTO().getAppointmentDate().equals(booking.getAppointmentDate())
                        && bookingExcelResponse.getBookingExcelDTO().getStartTime().equals(startTime)
                        && bookingExcelResponse.getBookingExcelDTO().getEndTime().equals(endTime)) {
                    invalidBookings.add(bookingExcelResponse);
                }
            });
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
                    WorkSchedule workSchedule = workScheduleRepository.getWorkScheduleByTime(
                            bookingExcelResponse.getBookingExcelDTO().getSpecializationName(), appointmentDate.getDayOfWeek(),
                            bookingExcelResponse.getBookingExcelDTO().getStartTime(), bookingExcelResponse.getBookingExcelDTO().getEndTime());
                    booking.setWorkSchedule(workSchedule);
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

}
