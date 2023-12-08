package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.ServiceCategoryDTO;
import khanhnq.project.clinicbookingmanagementsystem.dto.ServicesDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import java.util.List;

public interface AdminService {
    String approveRequestDoctor(Long userId);
    String rejectRequestDoctor(Long userId);
    UserResponse getAllUsers(int page, int size, String[] sorts);
    List<RequestDoctorResponse> getAllRequestDoctors();
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
}
