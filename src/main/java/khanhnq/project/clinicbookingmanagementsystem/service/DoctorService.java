package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.model.request.*;
import khanhnq.project.clinicbookingmanagementsystem.model.response.ResponseEntityBase;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DoctorService {
    ResponseEntityBase updateProfile(UserProfileRequest userProfileRequest, MultipartFile avatar);
    ResponseEntityBase getUserProfile();
    ResponseEntityBase getDoctorCareerInfo();
    ResponseEntityBase updateDoctorInformation(DoctorInformationRequest doctorInformationRequest, MultipartFile specialtyDegree);
    ResponseEntityBase registerWorkSchedules(RegisterWorkScheduleRequest registerWorkScheduleRequest);
    ResponseEntityBase confirmedBooking (Long bookingId);
    ResponseEntityBase cancelledBooking (Long bookingId);
    ResponseEntityBase completedBooking (Long bookingId);
    ResponseEntityBase getBookingDetail (Long bookingId);
    ResponseEntityBase getAllBookings(int page, int size, String[] sorts);
    ResponseEntityBase addMedicalRecord(Long bookingId, MedicalRecordRequest medicalRecordRequest);
    ResponseEntityBase updateMedicalRecord(Long medicalRecordId, MedicalRecordRequest medicalRecordRequest);
    ResponseEntityBase addLabResultsToMedicalRecord(List<LabResultRequest> labResultRequests);
    ResponseEntityBase addMedicalImagesToMedicalRecord(MedicalImageRequest medicalImageRequest, List<MultipartFile> medicalImageFiles);
    ResponseEntityBase getAllMedicalRecords();
    ResponseEntityBase getMedicalRecordByBookingId(Long bookingId);
}
