package khanhnq.project.clinicbookingmanagementsystem.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorTimeOffRequest {
    private LocalDateTime timeOffStart;
    private LocalDateTime timeOffEnd;
    private String reason;
}