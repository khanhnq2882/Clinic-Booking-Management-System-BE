package khanhnq.project.clinicbookingmanagementsystem.service;

import jakarta.mail.MessagingException;
import khanhnq.project.clinicbookingmanagementsystem.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.ServicesDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.SpecializationDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.UserDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.model.response.BookingResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.DoctorResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ServicesResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.UserResponse;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface AdminService {
    String resetPassword (String email) throws MessagingException;
    String unlockAccount (String username);
    UserResponse getAllUsers(int page, int size, String[] sorts);
    DoctorResponse getAllDoctors(int page, int size, String[] sorts);
    List<SpecializationDTO> getAllSpecializations();
    List<ServicesDTO> getServices();
    ServicesResponse getAllServices(int page, int size, String[] sorts);
    String addService(ServiceRequest serviceRequest);
    ServicesDTO getServiceById (Long serviceId);
    String updateService(ServiceRequest serviceRequest, Long serviceId);
    List<UserDTO> getUsers();
    List<BookingDTO> getBookings();
    ByteArrayInputStream exportUsersToExcel (List<UserDTO> users);
    ByteArrayInputStream exportServicesToExcel(List<ServicesDTO> services);
    ByteArrayInputStream exportBookingsToExcel (List<BookingDTO> bookings);
    List<Services> importServicesFromExcel (InputStream inputStream);
    String importBookingsFromExcel (InputStream inputStream);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}
