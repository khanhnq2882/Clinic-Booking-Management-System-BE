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
        if (this == o) return true;
        if (!(o instanceof WorkScheduleDTO that)) return false;
        return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }
}