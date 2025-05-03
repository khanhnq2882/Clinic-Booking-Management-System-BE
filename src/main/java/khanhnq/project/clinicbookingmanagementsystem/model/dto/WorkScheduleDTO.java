package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkScheduleDTO {
    private LocalTime startTime;
    private LocalTime endTime;
}