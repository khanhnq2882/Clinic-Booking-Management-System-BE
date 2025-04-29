package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkScheduleDTO {
    private LocalTime startTime;
    private LocalTime endTime;

    @Override
    public boolean equals(Object o) {
        if (Objects.isNull(o)) return false;
        WorkScheduleDTO workSchedule = (WorkScheduleDTO) o;
        return Objects.equals(this.getStartTime(), workSchedule.getStartTime())
                && Objects.equals(this.getEndTime(), workSchedule.getEndTime());
    }

}