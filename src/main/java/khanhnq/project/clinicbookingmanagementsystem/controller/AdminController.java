package khanhnq.project.clinicbookingmanagementsystem.controller;

import khanhnq.project.clinicbookingmanagementsystem.dto.ServiceCategoryDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.ServicesDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import khanhnq.project.clinicbookingmanagementsystem.entity.ServiceCategory;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.repository.ServiceCategoryRepository;
import khanhnq.project.clinicbookingmanagementsystem.repository.ServicesRepository;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl.CommonServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/admin", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AdminController {
    private final AdminService adminService;
    private final CommonServiceImpl commonService;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServicesRepository serviceRepository;

    @GetMapping("/get-all-users")
    public ResponseEntity<UserResponse> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "3") int size,
                                                    @RequestParam(defaultValue = "userId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllUsers(page, size, sorts));
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long fileId) {
        File file = commonService.getFileById(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }

    @GetMapping("/get-all-doctors")
    public ResponseEntity<DoctorResponse> getAllDoctors(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "3") int size,
                                                        @RequestParam(defaultValue = "userId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllDoctors(page, size, sorts));
    }

    @GetMapping("/get-all-specializations")
    public ResponseEntity<List<SpecializationDTO>> getAllSpecializations() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllSpecializations());
    }

    @GetMapping("/get-all-service-categories/{specializationId}")
    public ResponseEntity<List<ServiceCategoryDTO>> getAllServiceCategories(@PathVariable("specializationId") Long specializationId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getServiceCategories(specializationId));
    }

    @GetMapping("/get-all-service-categories")
    public ResponseEntity<ServiceCategoryResponse> getAllServiceCategories(@RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "3") int size,
                                                                           @RequestParam(defaultValue = "serviceCategoryId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllServiceCategories(page, size, sorts));
    }

    @PostMapping("/add-service-category")
    public ResponseEntity<String> addServiceCategory(@RequestBody ServiceCategoryRequest serviceCategoryRequest) {
        return MessageResponse.getResponseMessage(adminService.addServiceCategory(serviceCategoryRequest), HttpStatus.OK);
    }

    @GetMapping("/get-service-category/{serviceCategoryId}")
    public ResponseEntity<ServiceCategoryDTO> getServiceCategory(@PathVariable("serviceCategoryId") Long serviceCategoryId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getServiceCategoryById(serviceCategoryId));
    }

    @PostMapping("/update-service-category/{serviceCategoryId}")
    public ResponseEntity<String> updateServiceCategory(@PathVariable("serviceCategoryId") Long serviceCategoryId ,@RequestBody ServiceCategoryRequest serviceCategoryRequest) {
        return MessageResponse.getResponseMessage(adminService.updateServiceCategory(serviceCategoryRequest, serviceCategoryId), HttpStatus.OK);
    }

    @GetMapping("/get-all-services")
    public ResponseEntity<ServicesResponse> getAllServices(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "3") int size,
                                                           @RequestParam(defaultValue = "serviceId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllServices(page, size, sort));
    }

    @PostMapping("/add-service")
    public ResponseEntity<String> addService(@RequestBody ServiceRequest serviceRequest) {
        return MessageResponse.getResponseMessage(adminService.addService(serviceRequest), HttpStatus.OK);
    }

    @GetMapping("/get-service/{serviceId}")
    public ResponseEntity<ServicesDTO> getService(@PathVariable("serviceId") Long serviceId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getServiceById(serviceId));
    }

    @PostMapping("/update-service/{serviceId}")
    public ResponseEntity<String> updateService(@PathVariable("serviceId") Long serviceId ,@RequestBody ServiceRequest serviceRequest) {
        return MessageResponse.getResponseMessage(adminService.updateService(serviceRequest, serviceId), HttpStatus.OK);
    }

    @GetMapping("/export-users-to-excel")
    public ResponseEntity<InputStreamResource> exportUsersToExcel () {
        String fileName = "list_users.xlsx";
        InputStreamResource file = new InputStreamResource(adminService.exportUsersToExcel(adminService.getUsers()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/import-service-categories-from-excel")
    public ResponseEntity<String> importServiceCategoriesFromExcel (@RequestParam("file") MultipartFile file){
        try {
            commonService.checkExcelFormat(file);
            List<ServiceCategory> serviceCategories = adminService.importServiceCategoriesFromExcel(file.getInputStream());
            serviceCategoryRepository.saveAll(serviceCategories);
            return MessageResponse.getResponseMessage("Import data successfully.", HttpStatus.OK);
        } catch (IOException e) {
            return MessageResponse.getResponseMessage("Failed to store data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/import-services-from-excel")
    public ResponseEntity<String> importServicesFromExcel (@RequestParam("file") MultipartFile file){
        try {
            commonService.checkExcelFormat(file);
            List<Services> services = adminService.importServicesFromExcel(file.getInputStream());
            serviceRepository.saveAll(services);
            return MessageResponse.getResponseMessage("Import data successfully.", HttpStatus.OK);
        } catch (IOException e) {
            return MessageResponse.getResponseMessage("Failed to store data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/export-bookings-to-excel")
    public ResponseEntity<InputStreamResource> exportBookingsToExcel () {
        String fileName = "bookings.xlsx";
        InputStreamResource file = new InputStreamResource(adminService.exportBookingsToExcel(adminService.getBookings()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    // hien tai loi chi hien ve so dong duoc import, so dong k dc import chua chi ra loi cu the
    @PostMapping("/import-bookings-from-excel")
    public ResponseEntity<String> importBookingsFromExcel (@RequestParam("file") MultipartFile file){
        try {
            commonService.checkExcelFormat(file);
            return MessageResponse.getResponseMessage(adminService.importBookingsFromExcel(file.getInputStream()), HttpStatus.OK);
        } catch (IOException e) {
            return MessageResponse.getResponseMessage("Failed to store data from file excel.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-bookings")
    public ResponseEntity<BookingResponse> getBookings(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "3") int size,
                                                        @RequestParam(defaultValue = "bookingId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllBookings(page, size, sorts));
    }

}
