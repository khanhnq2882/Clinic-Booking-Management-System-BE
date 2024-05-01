package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

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
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CommonServiceImpl {

    private final UserRepository userRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final SpecializationRepository specializationRepository;
    private final FileService fileService;
    private final WardRepository wardRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;
    private final AddressRepository addressRepository;
    private final ServicesRepository servicesRepository;
    private final BookingRepository bookingRepository;
    private final AuthService authService;
    private final FileRepository fileRepository;

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
        } else {
            CreationHelper helper = workbook.getCreationHelper();
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(helper.createDataFormat().getFormat("dd-MM-yyyy"));
            cell.setCellValue((Date) value);
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

    public Map<Long, List<WorkSchedule>> groupWorkScheduleByDoctor() {
        Map<Long, List<WorkSchedule>> map = new HashMap<>();
        for (User user : userRepository.getDoctors()) {
            if (!map.containsKey(user.getUserId())) {
                List<WorkSchedule> workSchedules = workScheduleRepository.getWorkSchedulesByUserId(user.getUserId());
                map.put(user.getUserId(), workSchedules);
            }
        }
        return map;
    }

    public Map<Long, List<User>> groupDoctorsBySpecialization() {
        Map<Long, List<User>> map = new HashMap<>();
        for (Specialization specialization : specializationRepository.findAll()) {
            if (!map.containsKey(specialization.getSpecializationId())) {
                List<User> users = userRepository.getDoctorsBySpecializationId(specialization.getSpecializationId());
                map.put(specialization.getSpecializationId(), users);
            }
        }
        return map;
    }

    public String bookingCode() {
        Long maxServiceCode = Collections.max(bookingRepository.findAll()
                .stream()
                .map(booking -> Long.parseLong(booking.getBookingCode().substring(2)))
                .toList());
        return (bookingRepository.findAll().size() == 0) ? "BC1" : ("BC" + (++maxServiceCode));
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
                        )
                )
                .findFirst()
                .orElse(null);
        List<String> specificAddressElements = strings.stream()
                .filter(s -> !s.trim().equalsIgnoreCase(cityName) && !s.trim().equalsIgnoreCase(districtName) && !s.trim().equalsIgnoreCase(wardName)).toList();
        StringBuilder specificAddress = new StringBuilder();
        for (String element : specificAddressElements) {
            specificAddress.append(element+", ");
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
            services.setServiceCode(code.toString() + (++maxServiceCode));
        }
    }

//    public List<FileResponse> getAllFiles(Long userId) {
//        return fileService.loadFilesByUserId(userId).map(file -> {
//            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin/files/").path(file.getFileId().toString()).toUriString();
//            return new FileResponse(file.getFilePath().split("/")[1], file.getFilePath().split("/")[2], fileUrl);
//        }).collect(Collectors.toList());
//    }
//
//    public Set<WorkSchedule> filterWorkSchedules(Set<WorkSchedule> workSchedulesRequest, Long specializationId) {
//        Set<WorkSchedule> resultWorkSchedule = workSchedulesRequest;
//        for (User doctor : groupDoctorsBySpecialization().get(specializationId)) {
//            resultWorkSchedule = resultWorkSchedule.stream()
//                    .filter(request -> groupWorkScheduleByDoctor().get(doctor.getUserId()).stream()
//                            .noneMatch(workSchedule -> workSchedule.getStartTime().equals(request.getStartTime())
//                                    && workSchedule.getEndTime().equals(request.getEndTime())))
//                    .collect(Collectors.toSet());
//        }
//        return resultWorkSchedule;
//    }

    public AddressResponse getAddress(Booking booking) {
        Address address = addressRepository.findById(booking.getAddress().getAddressId()).orElse(null);
        new AddressResponse();
        return AddressResponse.builder()
                .addressId(Objects.requireNonNull(address).getAddressId())
                .specificAddress(address.getSpecificAddress())
                .wardName(address.getWard().getWardName())
                .districtName(address.getWard().getDistrict().getDistrictName())
                .cityName(address.getWard().getDistrict().getCity().getCityName())
                .build();
    }

    public String uploadFile(MultipartFile multipartFile, String typeImage) {
        try {
            User currentUser = authService.getCurrentUser();
            File file = new File();
            if (fileRepository.getFilesById(currentUser.getUserId()).stream().noneMatch(f -> f.getFilePath().split("/")[1].equals(typeImage))) {
                file.setFilePath(currentUser.getUsername()+"/"+typeImage+"/"+ StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename())));
                file.setData(multipartFile.getBytes());
                file.setUser(currentUser);
                file.setUpdatedBy(currentUser.getUsername());
                currentUser.getFiles().add(file);
            } else {
                file = fileRepository.getFileByType(typeImage, currentUser.getUserId());
                file.setFilePath(currentUser.getUsername()+"/"+typeImage+"/"+StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename())));
                file.setData(multipartFile.getBytes());
                file.setUser(currentUser);
                file.setUpdatedBy(currentUser.getUsername());
            }
            fileRepository.save(file);
            userRepository.save(currentUser);
            return "Uploaded " +typeImage+ " file successfully: " + multipartFile.getOriginalFilename();
        } catch (Exception e) {
            throw new FileUploadFailedException("Could not upload "+ typeImage+ " file: " + multipartFile.getOriginalFilename() + ". Error: " + e.getMessage());
        }
    }

    public void updateProfile (UserProfileRequest profileRequest, User currentUser) {
        userRepository.findAll().stream().filter(user -> Objects.nonNull(user.getPhoneNumber())).toList().forEach(user -> {
            if (profileRequest.getPhoneNumber().equals(user.getPhoneNumber()))
                throw new ResourceAlreadyExistException("Phone number", profileRequest.getPhoneNumber());
        });
        UserMapper.USER_MAPPER.mapToUser(currentUser, profileRequest);
        currentUser.setUpdatedBy(currentUser.getUsername());
        currentUser.setAddress(Address.builder()
                .specificAddress(profileRequest.getSpecificAddress())
                .ward(wardRepository.findById(profileRequest.getWardId()).orElse(null))
                .build());
    }
}
