package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;
import java.util.Date;
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
    private String universityName;
    private String clinicName;
    private String position;
    private String specialization;
    private Date startWork;
    private Date endWork;
    private String jobDescription;
    private Set<String> skillNames;
    private Set<FileResponse> fileResponses;
}
