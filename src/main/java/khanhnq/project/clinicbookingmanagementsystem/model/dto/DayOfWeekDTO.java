package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayOfWeekDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date workingDay;
    private Set<WorkScheduleDTO> workSchedules = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DayOfWeekDTO that)) return false;
        return Objects.equals(workingDay, that.workingDay) && Objects.equals(workSchedules, that.workSchedules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workingDay, workSchedules);
    }
}
