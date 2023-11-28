package khanhnq.project.clinicbookingmanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkScheduleDTO {
    private String startTime;
    private String endTime;
}