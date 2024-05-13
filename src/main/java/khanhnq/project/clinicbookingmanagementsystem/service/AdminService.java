package khanhnq.project.clinicbookingmanagementsystem.service;

import jakarta.mail.MessagingException;
import khanhnq.project.clinicbookingmanagementsystem.dto.*;
import khanhnq.project.clinicbookingmanagementsystem.entity.ServiceCategory;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface AdminService {
    String resetPassword (String email) throws MessagingException;
    UserResponse getAllUsers(int page, int size, String[] sorts);
    DoctorResponse getAllDoctors(int page, int size, String[] sorts);
    List<SpecializationDTO> getAllSpecializations();
    List<ServiceCategoryDTO> getServiceCategories(Long specializationId);
    ServiceCategoryResponse getAllServiceCategories(int page, int size, String[] sorts);
    String addServiceCategory(ServiceCategoryRequest serviceCategoryRequest);
    ServiceCategoryDTO getServiceCategoryById (Long serviceCategoryId);
    String updateServiceCategory(ServiceCategoryRequest serviceCategoryRequest, Long serviceCategoryId);
    ServicesResponse getAllServices(int page, int size, String[] sorts);
    String addService(ServiceRequest serviceRequest);
    ServicesDTO getServiceById (Long serviceId);
    String updateService(ServiceRequest serviceRequest, Long serviceId);
    List<UserDTO> getUsers();
    List<BookingDTO> getBookings();
    ByteArrayInputStream exportUsersToExcel (List<UserDTO> users);
    ByteArrayInputStream exportBookingsToExcel (List<BookingDTO> bookings);
    List<ServiceCategory> importServiceCategoriesFromExcel (InputStream inputStream);
    List<Services> importServicesFromExcel (InputStream inputStream);
    String importBookingsFromExcel (InputStream inputStream);
    BookingResponse getAllBookings(int page, int size, String[] sorts);
}
