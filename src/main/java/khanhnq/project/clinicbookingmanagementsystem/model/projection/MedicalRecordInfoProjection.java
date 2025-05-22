package khanhnq.project.clinicbookingmanagementsystem.model.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface MedicalRecordInfoProjection {
    // Booking info
    String getBookingCode();
    String getFirstName();
    String getLastName();
    LocalDate getDateOfBirth();
    String getGender();
    String getPhoneNumber();
    String getSpecificAddress();
    String getWardName();
    String getDistrictName();
    String getCityName();

    // Medical Record
    LocalDateTime getVisitDate();
    String getReasonForVisit();
    String getMedicalHistory();
    String getAllergies();
    String getDiagnosis();
    String getTreatmentPlan();
    String getPrescribedMedications();
    String getFollowUpInstructions();
    LocalDateTime getNextAppointmentDate();
    String getConsultationNotes();
    String getMedicalRecordStatus();

    // Lab Result
    LocalDateTime getSampleCollectionTime();
    LocalDateTime getSampleReceptionTime();
    LocalDateTime getTestDate();
    LocalDateTime getResultDeliveryDate();
    String getLabNote();
    String getLabStatus();

    // Doctor Info
    String getEducationLevel();
    String getDoctorFirstName();
    String getDoctorLastName();

    // Test Package
    String getTestPackageName();

    // Test Attribute
    String getTestAttributeName();
    String getTestAttributeUnit();
    String getAttributeMetadata();

    // Test Result
    String getTestResult();
    String getTestResultNote();
    String getTestResultStatus();

    // Normal Range
    Double getNormalMinValue();
    Double getNormalMaxValue();
    Double getNormalEqualValue();
    String getNormalExpectedValue();
    String getNormalRangeType();
}