package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.request.DoctorInformationRequest;
import org.springframework.http.ResponseEntity;

public interface DoctorService {
    ResponseEntity<String> addDoctorInformation(DoctorInformationRequest doctorInformationRequest);
}
