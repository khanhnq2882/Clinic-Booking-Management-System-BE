package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import java.time.LocalTime;
import java.util.Date;

public interface DoctorInfo {
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
    Date getWorkingDay();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
