package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.mapper.*;
import khanhnq.project.clinicbookingmanagementsystem.service.common.MethodsCommon;
import khanhnq.project.clinicbookingmanagementsystem.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
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
    private final SpecializationRepository specializationRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServicesRepository servicesRepository;
    private final BookingRepository bookingRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final MethodsCommon methodsCommon;
    private final Workbook workbook = new XSSFWorkbook();

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
                    String colName = serviceCategoryHeaderCellIndex(rows.get(0)).get(indexCell);
                    methodsCommon.checkBlankType(cells.get(indexCell), indexRow, colName);
                    switch (colName) {
                        case "Service Category Name" -> {
                            String serviceCategoryName = methodsCommon.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            List<String> serviceCategoriesName = serviceCategoryRepository.findAll().stream().map(ServiceCategory::getServiceCategoryName).toList();
                            if (serviceCategoriesName.stream().anyMatch(s -> s.equalsIgnoreCase(serviceCategoryName))) {
                                throw new ResourceException("Import data failed. Service category named '" + serviceCategoryName + "' is already existed.", HttpStatus.BAD_REQUEST);
                            }
                            serviceCategory.setServiceCategoryName(serviceCategoryName);
                        }
                        case "Description" ->
                                serviceCategory.setDescription(methodsCommon.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                        case "Specialization Name" -> {
                            String specializationName = methodsCommon.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            Specialization specialization = specializationRepository.getSpecializationBySpecializationName(specializationName);
                            if (Objects.isNull(specialization)) {
                                throw new ResourceException("Import failed. Specialization named '"+specializationName+"' in row " + indexRow + " is not exist.", HttpStatus.BAD_REQUEST);
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
                throw new ResourceException("Sheet named 'services' doesn't exist.", HttpStatus.BAD_REQUEST);
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                Services service = new Services();
                List<Cell> cells = methodsCommon.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    String colName = serviceHeaderCellIndex(rows.get(0)).get(indexCell);
                    methodsCommon.checkBlankType(cells.get(indexCell), indexRow, colName);
                    switch (colName) {
                        case "Service Name" -> {
                            String serviceName = methodsCommon.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue();
                            List<String> servicesName = servicesRepository.findAll().stream().map(Services::getServiceName).toList();
                            for (String name : servicesName) {
                                if (serviceName.equals(name)) {
                                    throw new ResourceException("Import data failed. Service named '" + serviceName + "' is already existed.", HttpStatus.BAD_REQUEST);
                                }
                            }
                            service.setServiceName(serviceName);
                        }
                        case "Price" ->
                                service.setPrice(methodsCommon.checkNumericType(cells.get(indexCell), indexRow, colName).getNumericCellValue());
                        case "Description" ->
                                service.setDescription(methodsCommon.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                        case "Service Category Name" -> {
                            ServiceCategory serviceCategory = serviceCategoryRepository.getServiceCategoriesByServiceCategoryName(methodsCommon.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
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
    public BookingImportResponse importBookingsFromExcel(InputStream inputStream) {
        try {
            List<BookingExcelDTO> bookingExcelList = new ArrayList<>();
            Sheet sheet = new XSSFWorkbook(inputStream).getSheet("bookings");
            if (sheet == null) {
                throw new ResourceException("Sheet named 'bookings' doesn't exist.", HttpStatus.BAD_REQUEST);
            }
            List<Row> rows = Lists.newArrayList(sheet.rowIterator());
            for (int indexRow = 1; indexRow < rows.size(); indexRow++) {
                BookingExcelDTO bookingExcel = new BookingExcelDTO();
                List<Cell> cells = methodsCommon.getAllCells(rows.get(indexRow));
                for (int indexCell = 0; indexCell < cells.size(); indexCell++) {
                    Map<Integer, String> headerCellIndex = bookingHeaderCellIndex(rows.get(0));
                    String colName = headerCellIndex.get(indexCell);
                    methodsCommon.checkBlankType(cells.get(indexCell), indexRow, colName);
                    switch (colName) {
                        case "Full Name" ->
                                checkFullName(cells.get(indexCell), indexRow, colName, bookingExcel);
                        case "Date Of Birth" ->
                                bookingExcel.setDateOfBirth(checkFormatDate(cells.get(indexCell), indexRow, colName));
                        case "Gender" ->
                                checkGender(cells.get(indexCell), indexRow, colName, bookingExcel);
                        case "Phone Number" ->
                                bookingExcel.setPhoneNumber(methodsCommon.getPhoneNumberFromExcel(cells.get(indexCell), indexRow, colName));
                        case "Address" ->
                                bookingExcel.setAddress(methodsCommon.getAddressFromExcel(cells.get(indexCell), indexRow, colName));
                        case "Appointment Date" ->
                                bookingExcel.setAppointmentDate(checkFormatDate(cells.get(indexCell), indexRow, colName));
                        case "Specialization" ->
                                checkSpecificationName(cells.get(indexCell), indexRow, colName, bookingExcel);
                        case "Start Time" ->
                                bookingExcel.setStartTime(methodsCommon.checkNumericType(cells.get(indexCell), indexRow, colName).getLocalDateTimeCellValue().toLocalTime());
                        case "End Time" ->
                                bookingExcel.setEndTime(methodsCommon.checkNumericType(cells.get(indexCell), indexRow, colName).getLocalDateTimeCellValue().toLocalTime());
                        case "Describe Symptoms" ->
                                bookingExcel.setDescribeSymptoms(methodsCommon.checkStringType(cells.get(indexCell), indexRow, colName).getStringCellValue());
                        default -> {
                        }
                    }
                }
                bookingExcelList.add(bookingExcel);
            }
            return filterExcelBookingList(bookingExcelList);
        } catch (IOException e) {
            throw new ResourceException("Failed to import data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new ResourceException("Invalid permissions.", HttpStatus.UNAUTHORIZED);
        }
        List<Booking> bookings = bookingRepository.findAll()
                .stream()
                .filter(booking -> Objects.isNull(booking.getUser()))
                .toList();
        return bookings.stream()
                .map(booking -> {
                    BookingDTO bookingDTO = BookingMapper.BOOKING_MAPPER.mapToBookingDTO(booking);
                    bookingDTO.setUserAddress(methodsCommon.getAddress(booking));
                    bookingDTO.setStartTime(booking.getWorkSchedule().getStartTime().toString());
                    bookingDTO.setEndTime(booking.getWorkSchedule().getEndTime().toString());
                    return bookingDTO;
                }).toList();
    }

    public Map<Integer, String> serviceCategoryHeaderCellIndex (Row row) {
        Map <Integer, String> headerCellIndex = new HashMap<>();
        List<Cell> cellsHeader = methodsCommon.getAllCells(row);
        for (int i = 0; i < cellsHeader.size(); i++) {
            switch (cellsHeader.get(i).getStringCellValue()) {
                case "Service Category Name" -> headerCellIndex.put(i, "Service Category Name");
                case "Description" -> headerCellIndex.put(i, "Description");
                case "Specialization Name" -> headerCellIndex.put(i, "Specialization Name");
                default -> {
                }
            }
        }
        return headerCellIndex;
    }

    public Map<Integer, String> serviceHeaderCellIndex (Row row) {
        Map <Integer, String> headerCellIndex = new HashMap<>();
        List<Cell> cellsHeader = methodsCommon.getAllCells(row);
        for (int i = 0; i < cellsHeader.size(); i++) {
            switch (cellsHeader.get(i).getStringCellValue()) {
                case "Service Name" -> headerCellIndex.put(i, "Service Name");
                case "Price" -> headerCellIndex.put(i, "Price");
                case "Description" -> headerCellIndex.put(i, "Description");
                case "Service Category Name" -> headerCellIndex.put(i, "Service Category Name");
                default -> {
                }
            }
        }
        return headerCellIndex;
    }

    public Map<Integer, String> bookingHeaderCellIndex (Row row) {
        Map <Integer, String> headerCellIndex = new HashMap<>();
        List<Cell> cellsHeader = methodsCommon.getAllCells(row);
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
        Date date = methodsCommon.checkDateType(cell, indexRow, colName).getDateCellValue();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (!dateFormat.format(date).matches("^\\d{4}-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$")) {
            throw new ResourceException("Import failed. "+ colName +" in row " + (indexRow + 1) + " must be yyyy-MM-dd format.", HttpStatus.BAD_REQUEST);
        }
        return date;
    }

    private void checkFullName (Cell cell, int indexRow, String colName, BookingExcelDTO bookingExcelDTO) {
        String fullName = methodsCommon.checkStringType(cell, indexRow, colName).getStringCellValue();
        bookingExcelDTO.setFirstName(fullName.split(" ")[0]);
        bookingExcelDTO.setLastName(fullName.substring(fullName.split(" ")[0].length() + 1));
    }

    private void checkGender (Cell cell, int indexRow, String colName, BookingExcelDTO bookingExcelDTO) {
        String gender = methodsCommon.checkStringType(cell, indexRow, colName).getStringCellValue();
        if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")) {
            throw new ResourceException("Import failed. "+ colName +" in row " + (indexRow + 1) + " must be 'Male' or 'Female'.", HttpStatus.BAD_REQUEST);
        }
        bookingExcelDTO.setGender(gender.equalsIgnoreCase("Male") ? 1 : 0);
    }

    private void checkSpecificationName (Cell cell, int indexRow, String colName, BookingExcelDTO bookingExcelDTO) {
        String specializationName = methodsCommon.checkStringType(cell, indexRow, colName).getStringCellValue();
        Specialization specialization = specializationRepository.getSpecializationBySpecializationName(specializationName);
        if (Objects.isNull(specialization)) {
            throw new ResourceException("Import failed. Specialization named " + specializationName + " is not exist.", HttpStatus.BAD_REQUEST);
        }
        bookingExcelDTO.setSpecializationName(specializationName);
    }

    public BookingImportResponse filterExcelBookingList (List<BookingExcelDTO> bookingExcelList) {
        BookingImportResponse bookingImportResponse = new BookingImportResponse();
        List<BookingExcelDTO> invalidBookings = new ArrayList<>();
        for (BookingExcelDTO bookingExcel : bookingExcelList) {
            Specialization specialization = specializationRepository.getSpecializationBySpecializationName(bookingExcel.getSpecializationName());
            User doctor = userRepository.getUserFromExcel(specialization.getSpecializationId(), bookingExcel.getStartTime(), bookingExcel.getEndTime());
            if (Objects.isNull(doctor)) {
                invalidBookings.add(bookingExcel);
                continue;
            }
            bookingRepository.getBookingsByDoctorId(doctor.getUserId()).forEach(booking -> {
                Date appointmentDate = booking.getAppointmentDate();
                LocalTime startTime = booking.getWorkSchedule().getStartTime();
                LocalTime endTime = booking.getWorkSchedule().getEndTime();
                if (bookingExcel.getAppointmentDate().equals(appointmentDate) && bookingExcel.getStartTime().equals(startTime) && bookingExcel.getEndTime().equals(endTime)) {
                    invalidBookings.add(bookingExcel);
                }
            });
        }
        bookingImportResponse.setInvalidBookings(convertToBookingList(invalidBookings));
        bookingExcelList.removeAll(invalidBookings);
        bookingImportResponse.setValidBookings(convertToBookingList(bookingExcelList));
        return bookingImportResponse;
    }

    public List<Booking> convertToBookingList (List<BookingExcelDTO> bookingExcelDTOS) {
        return bookingExcelDTOS.stream().map(bookingExcelDTO -> {
            Booking booking = BookingMapper.BOOKING_MAPPER.mapExcelToBooking(bookingExcelDTO);
            WorkSchedule workSchedule = workScheduleRepository.getWorkScheduleByTime(bookingExcelDTO.getSpecializationName(),bookingExcelDTO.getStartTime(), bookingExcelDTO.getEndTime());
            booking.setWorkSchedule(workSchedule);
            booking.setBookingCode(methodsCommon.bookingCode());
            return booking;
        }).toList();
    }

}
