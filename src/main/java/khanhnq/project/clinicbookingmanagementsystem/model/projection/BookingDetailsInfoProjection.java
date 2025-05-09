package khanhnq.project.clinicbookingmanagementsystem.model.projection;

public interface BookingDetailsInfoProjection {
    Long getBookingId();
    String getBookingCode();
    String getSpecializationName();
    String getFirstName();
    String getLastName();
    String getDateOfBirth();
    String getGender();
    String getPhoneNumber();
    String getUserAddress();
    String getDescribeSymptoms();
    String getWorkingDay();
    String getStartTime();
    String getEndTime();
    String getStatus();
    String getCreatedAt();
}