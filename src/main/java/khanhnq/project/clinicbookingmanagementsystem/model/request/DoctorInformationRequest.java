package khanhnq.project.clinicbookingmanagementsystem.model.request;

import jakarta.validation.constraints.NotNull;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.ExperienceDTO;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.SpecializationDTO;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorInformationRequest {
    private SpecializationDTO specialization;

    private Set<ExperienceDTO> workExperiences;

    @NotNull(message = "Career description can't be empty.")
    @Length(max = 255, message = "Career description has a maximum of 255 characters.")
    private String careerDescription;
}
