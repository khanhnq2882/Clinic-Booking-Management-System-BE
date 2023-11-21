package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;
import java.util.Date;
import java.util.List;

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
    private String universityName;
    private String status;
    // medical degree, medical license
    private String medicalDegreeType;
    private String medicalDegreeName;
    private String medicalDegreeUrl;
    private String medicalLicenseType;
    private String medicalLicenseName;
    private String medicalLicenseUrl;
    // experiences
    private List<ExperienceResponse> experiences;
}
