package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public interface BookingInfo {
    Long getBookingId();
    LocalDateTime getCreatedAt();
    Date getWorkingDay();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
