package khanhnq.project.clinicbookingmanagementsystem.model.projection;

public interface DoctorInfoProjection {
    Long getDoctorId();
    String getUserCode();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPhoneNumber();
    String getSpecializationName();
    String getEducationLevel();
    String getStatus();
    Long getFileId();
    String getFileType();
    String getFileName();
    String getFilePath();
    String getCreatedAt();
}
