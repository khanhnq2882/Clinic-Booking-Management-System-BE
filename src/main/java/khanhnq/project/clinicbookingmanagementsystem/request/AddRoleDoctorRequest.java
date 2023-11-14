package khanhnq.project.clinicbookingmanagementsystem.request;

import khanhnq.project.clinicbookingmanagementsystem.entity.Experience;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddRoleDoctorRequest {
    private String universityName;
    private MultipartFile medicalDegree;
    private MultipartFile medicalLicense;
    private List<ExperienceRequest> experiences;
}
