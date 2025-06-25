package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.mail.MessagingException;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ImagingServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.TestPackageRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.*;
import khanhnq.project.clinicbookingmanagementsystem.repository.ServicesRepository;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.service.AdminService;
import khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl.CommonServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/admin", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AdminController {

    private final AdminService adminService;
    private final CommonServiceImpl commonService;
    private final ServicesRepository serviceRepository;

    @PostMapping("/reset-password/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> resetPassword(@PathVariable("email") String email) throws MessagingException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.resetPassword(email));
    }

    @PostMapping("/unlock-account/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> unlockAccount(@PathVariable("username") String username){
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.unlockAccount(username));
    }

    @GetMapping("/get-all-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "3") int size,
                                                    @RequestParam(defaultValue = "userId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllUsers(page, size, sorts));
    }

    @GetMapping("/get-all-doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> getAllDoctors(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "3") int size,
                                                        @RequestParam(defaultValue = "doctorId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllDoctors(page, size, sorts));
    }

    @GetMapping("/get-bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> getBookings(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "3") int size,
                                                          @RequestParam(defaultValue = "bookingId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllBookings(page, size, sorts));
    }

    @GetMapping("/get-all-specializations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> getAllSpecializations() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllSpecializations());
    }

    @GetMapping("/get-all-services")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('USER')")
    public ResponseEntity<ResponseEntityBase> getAllServices(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "3") int size,
                                                           @RequestParam(defaultValue = "serviceId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllServices(page, size, sort));
    }

    @PostMapping("/add-service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> addService(@RequestBody ServiceRequest serviceRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.addService(serviceRequest));
    }

    @GetMapping("/get-service/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> getService(@PathVariable("serviceId") Long serviceId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getServiceById(serviceId));
    }

    @PostMapping("/update-service/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> updateService(@PathVariable("serviceId") Long serviceId ,@RequestBody ServiceRequest serviceRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.updateService(serviceRequest, serviceId));
    }

    @PostMapping("/add-test-package")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> addTestPackage(@RequestBody TestPackageRequest testPackageRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.addTestPackage(testPackageRequest));
    }

    @PostMapping("/update-test-package/{testPackageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> updateTestPackage(@PathVariable("testPackageId") Long testPackageId, @RequestBody TestPackageRequest testPackageRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.updateTestPackage(testPackageId, testPackageRequest));
    }

    @PostMapping("/update-test-package-status/{testPackageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> updateTestPackageStatus(@PathVariable("testPackageId") Long testPackageId, @RequestParam String status) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.updateTestPackageStatus(testPackageId, status));
    }

    @PostMapping("/add-imaging-service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> addImagingService(@RequestBody ImagingServiceRequest imagingServiceRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.addImagingService(imagingServiceRequest));
    }

    @PostMapping("/update-imaging-service/{imagingServiceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> updateImagingService(@PathVariable("imagingServiceId") Long imagingServiceId,
                                                                   @RequestBody ImagingServiceRequest imagingServiceRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.updateImagingService(imagingServiceId, imagingServiceRequest));
    }

    @PostMapping("/update-imaging-service-status/{imagingServiceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> updateImagingServiceStatus(@PathVariable("imagingServiceId") Long imagingServiceId,
                                                                         @RequestParam String status) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.updateImagingServiceStatus(imagingServiceId, status));
    }

    @GetMapping("/export-users-to-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> exportUsersToExcel() {
        String fileName = "Users.xlsx";
        InputStreamResource file = new InputStreamResource(adminService.exportUsersToExcel(adminService.getUsers()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/export-services-to-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> exportServicesToExcel() {
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        String fileName = "services_"+ currentDateTime + ".xlsx";
        InputStreamResource file = new InputStreamResource(adminService.exportServicesToExcel(adminService.getServices()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/import-services-from-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> importServicesFromExcel (@RequestParam("file") MultipartFile file){
        ResponseEntityBase response;
        try {
            commonService.checkExcelFormat(file);
            List<Services> services = adminService.importServicesFromExcel(file.getInputStream());
            serviceRepository.saveAll(services);
            response = new ResponseEntityBase(HttpStatus.OK.value(), null, "Import data successfully.");
        } catch (IOException e) {
            response = new ResponseEntityBase(HttpStatus.BAD_REQUEST.value(), "Failed to store data from file excel.", null);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @GetMapping("/export-bookings-to-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> exportBookingsToExcel () {
        String fileName = "bookings.xlsx";
        InputStreamResource file = new InputStreamResource(adminService.exportBookingsToExcel(new ArrayList<>()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/import-bookings-from-excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseEntityBase> importBookingsFromExcel (@RequestParam("file") MultipartFile file) {
        ResponseEntityBase response;
        try {
            commonService.checkExcelFormat(file);
            response = adminService.importBookingsFromExcel(file.getInputStream());
        } catch (IOException e) {
            response = new ResponseEntityBase(HttpStatus.BAD_REQUEST.value(), "Failed to store data from file excel.", null);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }


}
