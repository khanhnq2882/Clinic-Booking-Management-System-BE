package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.request.ServiceCategoryRequest;
import khanhnq.project.clinicbookingmanagementsystem.request.ServiceRequest;
import khanhnq.project.clinicbookingmanagementsystem.response.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface AdminService {
    ResponseEntity<String> approveRequestDoctor(Long userId);
    ResponseEntity<String> rejectRequestDoctor(Long userId);
    ResponseEntity<List<UserResponse>> getAllUsers();
    ResponseEntity<List<RequestDoctorResponse>> getAllRequestDoctors();
    ResponseEntity<List<DoctorResponse>> getAllDoctors();
    ResponseEntity<String> addServiceCategory(ServiceCategoryRequest serviceCategoryRequest);
    ResponseEntity<String> addService(ServiceRequest serviceRequest);
    ResponseEntity<List<SpecializationResponse>> getAllSpecializations();
    ResponseEntity<List<ServiceCategoryResponse>> getAllServiceCategories(Long specializationId);
    ResponseEntity<List<ServicesResponse>> getAllServices();

}
