package khanhnq.project.clinicbookingmanagementsystem.controller;

import jakarta.mail.MessagingException;
import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
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
    public ResponseEntity<ResponseEntityBase> resetPassword(@PathVariable("email") String email) throws MessagingException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.resetPassword(email));
    }

    @PostMapping("/unlock-account/{username}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseEntityBase> unlockAccount(@PathVariable("username") String username){
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.unlockAccount(username));
    }

    @GetMapping("/get-all-users")
    public ResponseEntity<ResponseEntityBase> getAllUsers(@RequestParam(defaultValue = "0") int page,
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
    public ResponseEntity<ResponseEntityBase> getAllDoctors(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "3") int size,
                                                        @RequestParam(defaultValue = "userId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllDoctors(page, size, sorts));
    }

    @GetMapping("/get-all-specializations")
    public ResponseEntity<ResponseEntityBase> getAllSpecializations() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllSpecializations());
    }

    @GetMapping("/get-all-services")
    public ResponseEntity<ResponseEntityBase> getAllServices(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "3") int size,
                                                           @RequestParam(defaultValue = "serviceId,asc") String[] sort) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllServices(page, size, sort));
    }

    @PostMapping("/add-service")
    public ResponseEntity<ResponseEntityBase> addService(@RequestBody ServiceRequest serviceRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.addService(serviceRequest));
    }

    @GetMapping("/get-service/{serviceId}")
    public ResponseEntity<ResponseEntityBase> getService(@PathVariable("serviceId") Long serviceId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getServiceById(serviceId));
    }

    @PostMapping("/update-service/{serviceId}")
    public ResponseEntity<ResponseEntityBase> updateService(@PathVariable("serviceId") Long serviceId ,@RequestBody ServiceRequest serviceRequest) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.updateService(serviceRequest, serviceId));
    }

    @GetMapping("/export-users-to-excel")
    public ResponseEntity<InputStreamResource> exportUsersToExcel() {
        String fileName = "Users.xlsx";
        InputStreamResource file = new InputStreamResource(adminService.exportUsersToExcel(adminService.getUsers()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/export-services-to-excel")
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
    public ResponseEntity<InputStreamResource> exportBookingsToExcel () {
        String fileName = "bookings.xlsx";
        InputStreamResource file = new InputStreamResource(adminService.exportBookingsToExcel(adminService.getBookings()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/import-bookings-from-excel")
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

    @GetMapping("/get-bookings")
    public ResponseEntity<ResponseEntityBase> getBookings(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "3") int size,
                                                       @RequestParam(defaultValue = "bookingId,asc") String[] sorts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(adminService.getAllBookings(page, size, sorts));
    }

}
