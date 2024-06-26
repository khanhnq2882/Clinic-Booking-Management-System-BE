package khanhnq.project.clinicbookingmanagementsystem.request;

import khanhnq.project.clinicbookingmanagementsystem.dto.WorkScheduleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWorkScheduleRequest {
    private Date workingDay;
    private int numberOfShiftsPerDay;
    private List<WorkScheduleDTO> workSchedules;
}
