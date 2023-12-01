package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.ServiceCategoryDTO;
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
    String addServiceCategory(ServiceCategoryRequest serviceCategoryRequest);
    String addService(ServiceRequest serviceRequest);
    List<SpecializationResponse> getAllSpecializations();
    List<ServiceCategoryDTO> getServiceCategories(Long specializationId);
    ServiceCategoryResponse getAllServiceCategories(int page, int size, String[] sorts);
    ServicesResponse getAllServices(int page, int size, String[] sorts);
}
