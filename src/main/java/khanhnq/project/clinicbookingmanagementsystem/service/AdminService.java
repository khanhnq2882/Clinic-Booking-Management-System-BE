package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.BookingDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.ServiceCategoryDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.ServicesDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.UserDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Booking;
import khanhnq.project.clinicbookingmanagementsystem.entity.ServiceCategory;
import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface AdminService {
    UserResponse getAllUsers(int page, int size, String[] sorts);
    DoctorResponse getAllDoctors(int page, int size, String[] sorts);
    List<SpecializationResponse> getAllSpecializations();
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
    ByteArrayInputStream exportUsersToExcel (List<UserDTO> users);
    List<ServiceCategory> importServiceCategoriesFromExcel (InputStream inputStream);
    List<Services> importServicesFromExcel (InputStream inputStream);
    List<Booking> importBookingsFromExcel (InputStream inputStream);
    List<BookingDTO> getAllBookings();
}
