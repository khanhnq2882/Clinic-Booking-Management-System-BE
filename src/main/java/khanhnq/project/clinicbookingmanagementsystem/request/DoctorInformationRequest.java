package khanhnq.project.clinicbookingmanagementsystem.request;

import khanhnq.project.clinicbookingmanagementsystem.dto.ExperienceDTO;
import khanhnq.project.clinicbookingmanagementsystem.entity.Specialization;
import lombok.*;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorInformationRequest {
    private Specialization specialization;
    private Set<ExperienceDTO> workExperiences;
    private String careerDescription;
}
