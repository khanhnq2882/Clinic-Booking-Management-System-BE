package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorInformationRequest {
    private String specialization;
    private Set<WorkScheduleRequest> workSchedules;
}
