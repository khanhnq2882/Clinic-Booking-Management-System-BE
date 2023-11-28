package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.dto.ServiceCategoryDTO;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface AdminService {
    ResponseEntity<String> approveRequestDoctor(Long userId);
    ResponseEntity<String> rejectRequestDoctor(Long userId);
    UserResponse getAllUsers(int page, int size, String[] sorts);
    List<RequestDoctorResponse> getAllRequestDoctors();
    DoctorResponse getAllDoctors(int page, int size, String[] sorts);
    ResponseEntity<String> addServiceCategory(ServiceCategoryRequest serviceCategoryRequest);
    ResponseEntity<String> addService(ServiceRequest serviceRequest);
    List<SpecializationResponse> getAllSpecializations();
    List<ServiceCategoryDTO> getServiceCategories(Long specializationId);
    ServiceCategoryResponse getAllServiceCategories(int page, int size, String[] sorts);
    ServicesResponse getAllServices(int page, int size, String[] sorts);

}
