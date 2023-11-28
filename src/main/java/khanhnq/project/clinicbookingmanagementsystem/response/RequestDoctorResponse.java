package khanhnq.project.clinicbookingmanagementsystem.response;

import khanhnq.project.clinicbookingmanagementsystem.dto.ExperienceDTO;
import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestDoctorResponse {
    private Long userId;
    private String userCode;
    private String email;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private int gender;
    private String phoneNumber;
    private Set<String> roleNames;
    private String universityName;
    private String status;
    private String medicalDegreeType;
    private String medicalDegreeName;
    private String medicalDegreeUrl;
    private String medicalLicenseType;
    private String medicalLicenseName;
    private String medicalLicenseUrl;
    private List<ExperienceDTO> doctorExperiences;
}
