package khanhnq.project.clinicbookingmanagementsystem.model.dto.projection;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public interface BookingTimeInfoProjection {
    Long getBookingId();
    LocalDateTime getCreatedAt();
    Date getWorkingDay();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
