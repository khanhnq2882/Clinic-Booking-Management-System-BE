package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddRoleDoctorRequest {
    private String universityName;
    private List<ExperienceRequest> experiences;
}
