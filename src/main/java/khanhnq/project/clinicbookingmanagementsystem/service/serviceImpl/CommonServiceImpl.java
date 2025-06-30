package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import khanhnq.project.clinicbookingmanagementsystem.common.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.exception.SystemException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.DoctorMapper;
import khanhnq.project.clinicbookingmanagementsystem.mapper.WorkExperienceMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.exception.BadRequestException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceAlreadyExistException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.model.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.AddressResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class CommonServiceImpl {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private WardRepository wardRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private AmazonS3 s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String awsS3Region;

    public FileResponse getFileFromS3(String fileType, String fileName, String filePath) {
        String fileS3Url = "https://" +bucketName+ ".s3." +awsS3Region+ ".amazonaws.com/" + filePath;
        return new FileResponse(fileType, fileName, fileS3Url);
    }

    public UserDTO getUserDetails(User user) {
        UserDTO userDTO = UserMapper.USER_MAPPER.mapToUserDTO(user);
        if (user.getAddress() != null) {
            userDTO.setUserAddress(getAddress(user));
        }
        String gender;
        if (userDTO.getGender() != null) {
            gender = user.getGender() == 1 ? "Male" : "Female";
            userDTO.setGender(gender);
        }
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
            FileResponse fileResponse = getFileFromS3(file.getFileType(), file.getFileName(), file.getFilePath());
            userDTO.setAvatar(fileResponse);
        }
        return userDTO;
    }

    public DoctorDetailsDTO getDoctorDetails(Long doctorId) {
        List<DoctorDetailsInfoProjection> doctorDetails = doctorRepository.getDoctorDetails(doctorId);
        Map<Long, DoctorDetailsDTO> doctorMap = new HashMap<>();
        doctorDetails.forEach(doctorDetailsInfoProjection -> {
            DoctorDetailsDTO doctorDetailsDTO = doctorMap.getOrDefault(doctorId, new DoctorDetailsDTO());
            DoctorMapper.DOCTOR_MAPPER.mapToDoctorDetailsDTO(doctorDetailsDTO, doctorDetailsInfoProjection);
            if (doctorDetailsInfoProjection.getPosition() != null || doctorDetailsInfoProjection.getWorkSpecializationName() != null ||
                    doctorDetailsInfoProjection.getWorkPlace() != null || doctorDetailsInfoProjection.getYearOfStartWork() != null ||
                    doctorDetailsInfoProjection.getYearOfEndWork() != null || doctorDetailsInfoProjection.getDescription() != null) {
                WorkExperienceDTO workExperienceDTO =
                        WorkExperienceMapper.WORK_EXPERIENCE_MAPPER.mapToWorkExperienceDTO(doctorDetailsInfoProjection);
                doctorDetailsDTO.getWorkExperiences().add(workExperienceDTO);
            }
            if (doctorDetailsInfoProjection.getFileId() != null) {
                FileResponse fileResponse = getFileFromS3(doctorDetailsInfoProjection.getFileType(),
                        doctorDetailsInfoProjection.getFileName(),
                        doctorDetailsInfoProjection.getFilePath());
                doctorDetailsDTO.getFiles().add(fileResponse);
            }
            if (doctorDetailsInfoProjection.getWorkingDay() != null) {
                Set<DayOfWeekDTO> daysOfWeek = doctorDetailsDTO.getDaysOfWeek();
                getDayOfWeekDetails(doctorDetailsInfoProjection, daysOfWeek);
            }
            doctorMap.put(doctorId, doctorDetailsDTO);
        });
        return doctorMap.get(doctorId);
    }

    public void getDayOfWeekDetails(DoctorDetailsInfoProjection doctorDetailsInfoProjection, Set<DayOfWeekDTO> daysOfWeek) {
        DayOfWeekDTO existingDay = daysOfWeek.stream()
                .filter(d -> d.getWorkingDay().equals(doctorDetailsInfoProjection.getWorkingDay()))
                .findFirst()
                .orElse(null);
        WorkScheduleDTO workScheduleDTO = new WorkScheduleDTO();
        workScheduleDTO.setStartTime(doctorDetailsInfoProjection.getStartTime());
        workScheduleDTO.setEndTime(doctorDetailsInfoProjection.getEndTime());
        if (existingDay != null) {
            existingDay.getWorkSchedules().add(workScheduleDTO);
            Set<WorkScheduleDTO> sorted = existingDay.getWorkSchedules().stream()
                    .sorted(Comparator.comparing(WorkScheduleDTO::getStartTime))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            existingDay.setWorkSchedules(sorted);
        } else {
            DayOfWeekDTO newDay = new DayOfWeekDTO();
            newDay.setWorkingDay(doctorDetailsInfoProjection.getWorkingDay());
            Set<WorkScheduleDTO> sorted = Stream.of(workScheduleDTO)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            newDay.setWorkSchedules(sorted);
            daysOfWeek.add(newDay);
        }
    }

    public BigDecimal validateAndConvertAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new BadRequestException("Amount can't be empty.");
        }
        String cleaned = amountStr.replace(",", "").trim();
        if (!cleaned.matches("^\\d+(\\.\\d{1,2})?$")) {
            throw new BadRequestException("Invalid amount. Amount only allows up to 2 decimal places.");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Amount is not convertible.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Amount is not negative.");
        }
        return amount;
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

    public Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    public List<Cell> getAllCells (Row row) {
        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < row.getLastCellNum(); x++) {
            Cell cell = row.getCell(x, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cells.add(cell);
        }
        return cells;
    }

    public void createHeader(Workbook workbook, Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            Font font = workbook.createFont();
            font.setBold(true);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
        }
    }

    public void createCell(Workbook workbook, Row row, int cellIndex, Object value) {
        Cell cell = row.createCell(cellIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date){
            CreationHelper helper = workbook.getCreationHelper();
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(helper.createDataFormat().getFormat("dd-MM-yyyy"));
            cell.setCellValue((Date) value);
            cell.setCellStyle(cellStyle);
        } else {
            CreationHelper helper = workbook.getCreationHelper();
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(helper.createDataFormat().getFormat("HH:mm"));
            cell.setCellValue(LocalDateTime.from((LocalTime) value));
            cell.setCellStyle(cellStyle);
        }
    }

    public Cell checkBlankType (Cell cell, int rowIndex, String colName) {
        if (cell.getCellType() == CellType.BLANK) {
            throw new BadRequestException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " can't be blank.");
        }
        return cell;
    }

    public Cell checkStringType (Cell cell, int rowIndex, String colName) {
        if (!cell.getCellType().equals(CellType.STRING)) {
            throw new BadRequestException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " must be string type.");
        }
        if (cell.getStringCellValue().length() > 255) {
            throw new BadRequestException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " has a maximum of 255 characters.");
        }
        return cell;
    }

    public Cell checkNumericType (Cell cell, int rowIndex, String colName) {
        if (!cell.getCellType().equals(CellType.NUMERIC)) {
            throw new BadRequestException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " must be numeric type.");
        }
        return cell;
    }

    public Cell checkDateType (Cell cell, int rowIndex, String colName) {
        if (!DateUtil.isCellDateFormatted(checkNumericType(cell, rowIndex, colName))) {
            throw new BadRequestException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " must be date type.");
        }
        return cell;
    }

    public String bookingCode() {
        StringBuilder bookingCode = new StringBuilder();
        if (bookingRepository.findAll().size() == 0) {
            bookingCode.append("BC1");
        } else {
            Long maxBookingCode = Collections.max(bookingRepository.findAll()
                    .stream()
                    .map(booking -> Long.parseLong(booking.getBookingCode().substring(2)))
                    .toList());
            bookingCode.append("BC").append(++maxBookingCode);
        }
        return bookingCode.toString();
    }

    public String getPhoneNumberFromExcel (Cell cell, int indexRow, String colName) {
        String phoneNumber = checkStringType(cell, indexRow, colName).getStringCellValue();
        if (!phoneNumber.matches("^0[2|3|5|7|8|9][0-9]{8}$")) {
            throw new BadRequestException("Import failed. Phone number is in wrong format.");
        }
        return phoneNumber;
    }

    public Address getAddressFromExcel (Cell cell, int indexRow, String colName) {
        List<String> strings = Arrays.asList(checkStringType(cell, indexRow, colName).getStringCellValue().split(","));
        if (strings.size() < 3) {
            throw new BadRequestException("Invalid address. Must contain at least information about wards, districts, and cities of Vietnam and separated by commas.");
        }
        String wardName = strings.get(strings.size() - 3).trim();
        String districtName = strings.get(strings.size() - 2).trim();
        String cityName = strings.get(strings.size() - 1).trim();
        List<Ward> wards = wardRepository.getWardsByWardName(wardName);
        if (wards.size() == 0) {
            throw new BadRequestException("Ward named '"+ wardName +"' of column "+ colName +", row "+ indexRow +" doesn't exist.");
        }
        List<District> districts = districtRepository.getDistrictsByDistrictName(districtName);
        if (districts.size() == 0) {
            throw new BadRequestException("District named '"+ districtName +"' of column "+ colName +", row "+ indexRow +" doesn't exist.");
        }
        List<City> cities = cityRepository.getCitiesByCityName(cityName);
        if (cities.size() == 0) {
            throw new BadRequestException("City named '"+ cityName +"' of column "+ colName +", row "+ indexRow +" doesn't exist.");
        }
        Address address = wards.stream()
                .flatMap(ward -> districts.stream()
                        .filter(district -> ward.getDistrict().getDistrictName().equals(district.getDistrictName()))
                        .flatMap(district -> cities.stream()
                                .filter(city -> district.getCity().getCityName().equals(city.getCityName()))
                                .map(city -> {
                                    Address newAddress = new Address();
                                    newAddress.setWard(ward);
                                    return newAddress;
                                })
                        )).findFirst().orElse(null);
        List<String> specificAddressElements = strings.stream()
                .filter(s -> !s.trim().equals(cityName) && !s.trim().equals(districtName) && !s.trim().equals(wardName)).toList();
        StringBuilder specificAddress = new StringBuilder();
        for (int i=0; i<specificAddressElements.size(); i++) {
            specificAddress.append(specificAddressElements.get(i)).append((i != specificAddressElements.size()-1) ? ", " : "");
        }
        address.setSpecificAddress(specificAddress.toString());
        return address;
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

    public void uploadFile(MultipartFile multipartFile, String fileType, User currentUser) {
        String filePath = "";
        try {
            String folderPath = currentUser.getUsername() + "/" + fileType + "/";
            ListObjectsV2Request listRequest = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(folderPath);
            ListObjectsV2Result listing = s3Client.listObjectsV2(listRequest);
            if (listing.getObjectSummaries().size() > 0) {
                for (S3ObjectSummary summary : listing.getObjectSummaries())
                    s3Client.deleteObject(bucketName, summary.getKey());
            }
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = folderPath + multipartFile.getOriginalFilename();
            s3Client.putObject(bucketName, filePath, multipartFile.getInputStream(), objectMetadata);
            File file = new File();
            file.setFileName(StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename())));
            file.setFileType(fileType);
            file.setFilePath(filePath);
            file.setFileSize(multipartFile.getSize());
            file.setUser(currentUser);
            if (fileRepository.getFilesById(currentUser.getUserId()).stream().anyMatch(f -> f.getFileType().equals(fileType))) {
                fileRepository.deleteFileByFileType(currentUser.getUserId(), fileType);
                file.setUpdatedBy(currentUser.getUsername());
            } else {
                file.setCreatedBy(currentUser.getUsername());
            }
            fileRepository.save(file);
        } catch (Exception e) {
            try {
                s3Client.deleteObject(bucketName, filePath);
            } catch (AmazonClientException deleteEx) {
                log.error("S3 rollback failed: {}", deleteEx.getMessage());
            }
            throw new SystemException(MessageConstants.ERROR_SAVE_OR_ROLLBACK_MEDICAL_IMAGE_TO_S3);
        }
    }

    public void updateProfile (UserProfileRequest profileRequest, User currentUser, MultipartFile avatar) {
        List<User> users = userRepository.findAll()
                .stream()
                .filter(user -> Objects.nonNull(user.getPhoneNumber()) &&
                        !user.getUserId().equals(currentUser.getUserId()))
                .toList();
        users.forEach(user -> {
            if (profileRequest.getPhoneNumber().equals(user.getPhoneNumber()))
                throw new ResourceAlreadyExistException("Phone number", profileRequest.getPhoneNumber());
        });
        UserMapper.USER_MAPPER.mapToUser(currentUser, profileRequest);
        currentUser.setUpdatedBy(currentUser.getUsername());
        Address address = Address.builder()
                .specificAddress(profileRequest.getSpecificAddress())
                .ward(wardRepository.findById(profileRequest.getWardId()).orElse(null))
                .build();
        address.setCreatedBy(currentUser.getUsername());
        currentUser.setAddress(address);
        uploadFile(avatar, "avatar", currentUser);
    }

    public void checkExcelFormat(MultipartFile file) {
        String excelType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (!excelType.equals(file.getContentType())) {
            throw new BadRequestException(MessageConstants.INVALID_EXCEL_FORMAT);
        }
    }

    public BookingResponse getAllBookings(Page<BookingDetailsInfoProjection> bookingDetailsPage) {
        List<BookingDetailsInfoProjection> bookings = bookingDetailsPage.getContent();
        return BookingResponse.builder()
                .totalItems(bookingDetailsPage.getTotalElements())
                .totalPages(bookingDetailsPage.getTotalPages())
                .currentPage(bookingDetailsPage.getNumber())
                .bookings(bookings)
                .build();
    }
}
