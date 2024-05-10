package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.constant.MessageConstants;
import khanhnq.project.clinicbookingmanagementsystem.entity.*;
import khanhnq.project.clinicbookingmanagementsystem.exception.BusinessException;
import khanhnq.project.clinicbookingmanagementsystem.exception.FileUploadFailedException;
import khanhnq.project.clinicbookingmanagementsystem.exception.ResourceAlreadyExistException;
import khanhnq.project.clinicbookingmanagementsystem.mapper.UserMapper;
import khanhnq.project.clinicbookingmanagementsystem.repository.*;
import khanhnq.project.clinicbookingmanagementsystem.request.UserProfileRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.AddressResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.FileResponse;
import khanhnq.project.clinicbookingmanagementsystem.service.AuthService;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class CommonServiceImpl {

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final AddressRepository addressRepository;
    private final ServicesRepository servicesRepository;
    private final BookingRepository bookingRepository;
    private final AuthService authService;
    private final FileRepository fileRepository;

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
        int countCells = row.getLastCellNum();
        for (int x = 0; x < countCells; x++) {
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
            throw new BusinessException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " can't be blank.");
        }
        return cell;
    }

    public Cell checkStringType (Cell cell, int rowIndex, String colName) {
        if (!cell.getCellType().equals(CellType.STRING)) {
            throw new BusinessException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " must be string type.");
        }
        if (cell.getStringCellValue().length() > 255) {
            throw new BusinessException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " has a maximum of 255 characters.");
        }
        return cell;
    }

    public Cell checkNumericType (Cell cell, int rowIndex, String colName) {
        if (!cell.getCellType().equals(CellType.NUMERIC)) {
            throw new BusinessException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " must be numeric type.");
        }
        return cell;
    }

    public Cell checkDateType (Cell cell, int rowIndex, String colName) {
        if (!DateUtil.isCellDateFormatted(checkNumericType(cell, rowIndex, colName))) {
            throw new BusinessException("Import data failed. The value of column named '" + colName + "' in row " + (rowIndex + 1) + " must be date type.");
        }
        return cell;
    }

    public String bookingCode() {
        StringBuilder bookingCode = new StringBuilder();
        if (bookingRepository.findAll().size() == 0) {
            bookingCode.append("BC1");
        } else {
            Long maxServiceCode = Collections.max(bookingRepository.findAll()
                    .stream()
                    .map(booking -> Long.parseLong(booking.getBookingCode().substring(2)))
                    .toList());
            bookingCode.append("BC").append(++maxServiceCode);
        }
        return bookingCode.toString();
    }

    public String getPhoneNumberFromExcel (Cell cell, int indexRow, String colName) {
        String phoneNumber = checkStringType(cell, indexRow, colName).getStringCellValue();
        if (!phoneNumber.matches("^0[2|3|5|7|8|9][0-9]{8}$")) {
            throw new BusinessException("Import failed. Phone number is in wrong format.");
        }
        return phoneNumber;
    }

    public Address getAddressFromExcel (Cell cell, int indexRow, String colName) {
        List<String> strings = Arrays.asList(checkStringType(cell, indexRow, colName).getStringCellValue().split(","));
        if (strings.size() < 3) {
            throw new BusinessException("Invalid address. Must contain at least information about wards, districts, and cities of Vietnam and separated by commas.");
        }
        String wardName = strings.get(strings.size() - 3).trim();
        String districtName = strings.get(strings.size() - 2).trim();
        String cityName = strings.get(strings.size() - 1).trim();
        List<Ward> wards = wardRepository.getWardsByWardName(wardName);
        if (wards.size() == 0) {
            throw new BusinessException("Ward named '"+ wardName +"' of column "+ colName +", row "+ indexRow +" doesn't exist.");
        }
        List<District> districts = districtRepository.getDistrictsByDistrictName(districtName);
        if (districts.size() == 0) {
            throw new BusinessException("District named '"+ districtName +"' of column "+ colName +", row "+ indexRow +" doesn't exist.");
        }
        List<City> cities = cityRepository.getCitiesByCityName(cityName);
        if (cities.size() == 0) {
            throw new BusinessException("City named '"+ cityName +"' of column "+ colName +", row "+ indexRow +" doesn't exist.");
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

    public void serviceCode(Services services, ServiceCategory serviceCategory) {
        StringBuilder code = new StringBuilder();
        for (String s : serviceCategory.getServiceCategoryName().split(" ")) {
            code.append(s.charAt(0));
        }
        List<Services> servicesList = servicesRepository.getServicesByCode(code.toString());
        if (servicesList.size() == 0) {
            services.setServiceCode(code.append("1").toString());
        } else {
            String s = code.toString();
            Long maxServiceCode = Collections.max(servicesList
                    .stream()
                    .map(service -> Long.parseLong(service.getServiceCode().substring(s.length())))
                    .toList());
            services.setServiceCode(code.append(++maxServiceCode).toString());
        }
    }

    public List<FileResponse> getAllFiles(Long userId) {
        return loadFilesByUserId(userId).map(file -> {
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(file.getFileId().toString()).toUriString();
            return new FileResponse(file.getFileType(), file.getFileName(), fileUrl);
        }).toList();
    }

    public AddressResponse getAddress(Booking booking) {
        Address address = addressRepository.findById(booking.getAddress().getAddressId()).orElse(null);
        return AddressResponse.builder()
                .addressId(Objects.requireNonNull(address).getAddressId())
                .specificAddress(address.getSpecificAddress())
                .wardName(address.getWard().getWardName())
                .districtName(address.getWard().getDistrict().getDistrictName())
                .cityName(address.getWard().getDistrict().getCity().getCityName())
                .build();
    }

    public String uploadFile(MultipartFile multipartFile, String fileType) {
        try {
            User currentUser = authService.getCurrentUser();
            File file = new File();
            if (fileRepository.getFilesById(currentUser.getUserId()).stream().noneMatch(f -> f.getFileType().equals(fileType))) {
                file.setFileName(StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename())));
                file.setFileType(fileType);
                file.setData(multipartFile.getBytes());
                file.setUser(currentUser);
                file.setCreatedBy(currentUser.getUsername());
                currentUser.getFiles().add(file);
            } else {
                file = fileRepository.getFileByType(currentUser.getUserId(), fileType);
                file.setFileName(StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename())));
                file.setFileType(fileType);
                file.setData(multipartFile.getBytes());
                file.setUser(currentUser);
                file.setUpdatedBy(currentUser.getUsername());
            }
            fileRepository.save(file);
            userRepository.save(currentUser);
            return "Uploaded " +fileType+ " file successfully: " + multipartFile.getOriginalFilename();
        } catch (Exception e) {
            throw new FileUploadFailedException("Could not upload "+ fileType+ " file: " + multipartFile.getOriginalFilename() + ". Error: " + e.getMessage());
        }
    }

    public void updateProfile (UserProfileRequest profileRequest, User currentUser) {
        userRepository.findAll().stream().filter(user -> Objects.nonNull(user.getPhoneNumber())).toList().forEach(user -> {
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
    }

    public Stream<File> loadFilesByUserId(Long userId) {
        return fileRepository.getFilesById(userId).stream();
    }

    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId).get();
    }

    public void checkExcelFormat(MultipartFile file) {
        String excelType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (!excelType.equals(file.getContentType())) {
            throw new BusinessException(MessageConstants.INVALID_EXCEL_FORMAT);
        }
    }
}
