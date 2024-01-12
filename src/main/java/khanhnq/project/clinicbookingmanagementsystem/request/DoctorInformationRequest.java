package khanhnq.project.clinicbookingmanagementsystem.request;

import khanhnq.project.clinicbookingmanagementsystem.dto.ExperienceDTO;
import lombok.*;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorInformationRequest {
    private UserProfileRequest profileRequest;
    private Set<ExperienceDTO> workExperiences;
    private String professionalDescription;
}
