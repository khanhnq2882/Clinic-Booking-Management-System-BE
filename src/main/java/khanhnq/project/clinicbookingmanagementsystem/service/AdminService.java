package khanhnq.project.clinicbookingmanagementsystem.service;

import jakarta.mail.MessagingException;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.ServicesDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.UserDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ImagingServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.request.TestPackageRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface AdminService {
    ResponseEntityBase resetPassword (String email) throws MessagingException;
    ResponseEntityBase unlockAccount (String username);
    ResponseEntityBase getAllUsers(int page, int size, String[] sorts);
    ResponseEntityBase getAllDoctors(int page, int size, String[] sorts);
    ResponseEntityBase getAllSpecializations();
    List<ServicesDTO> getServices();
    ResponseEntityBase getAllServices(int page, int size, String[] sorts);
    ResponseEntityBase addService(ServiceRequest serviceRequest);
    ResponseEntityBase getServiceById (Long serviceId);
    ResponseEntityBase updateService(ServiceRequest serviceRequest, Long serviceId);
    List<UserDTO> getUsers();
    ByteArrayInputStream exportUsersToExcel (List<UserDTO> users);
    ByteArrayInputStream exportServicesToExcel(List<ServicesDTO> services);
    ByteArrayInputStream exportBookingsToExcel (List<BookingDetailsInfoProjection> bookings);
    List<Services> importServicesFromExcel (InputStream inputStream);
    ResponseEntityBase importBookingsFromExcel (InputStream inputStream);
    ResponseEntityBase getAllBookings(int page, int size, String[] sorts);
    ResponseEntityBase addTestPackage(TestPackageRequest testPackageRequest);
    ResponseEntityBase updateTestPackage(Long testPackageId, TestPackageRequest testPackageRequest);
    ResponseEntityBase updateTestPackageStatus(Long testPackageId, String status);
    ResponseEntityBase addImagingService(ImagingServiceRequest imagingServiceRequest);
    ResponseEntityBase updateImagingService(Long imagingServiceId, ImagingServiceRequest imagingServiceRequest);
    ResponseEntityBase updateImagingServiceStatus(Long imagingServiceId, String status);
}
