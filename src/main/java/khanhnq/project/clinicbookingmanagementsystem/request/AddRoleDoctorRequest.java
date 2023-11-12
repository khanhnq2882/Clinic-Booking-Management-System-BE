package khanhnq.project.clinicbookingmanagementsystem.request;

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
    //experience
    private String clinicName;
    private String position;
    private String startWork;
    private String endWork;
    private Long specializationId;
    private List<Long> skillIds;
    private String jobDescription;
}
