package khanhnq.project.clinicbookingmanagementsystem.model.projection;

import java.time.LocalTime;
import java.util.Date;

public interface DoctorDetailsInfoProjection {
    Long getDoctorId();
    String getUserCode();
    String getFirstName();
    String getLastName();
    String getSpecializationName();
    String getEducationLevel();
    String getBiography();
    String getCareerDescription();
    String getPosition();
    String getWorkSpecializationName();
    String getWorkPlace();
    Integer getYearOfStartWork();
    Integer getYearOfEndWork();
    String getDescription();
    Long getFileId();
    String getFileType();
    String getFileName();
    String getFilePath();
    Date getWorkingDay();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
