package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkScheduleResponse {
    private String startTime;
    private String endTime;
}
