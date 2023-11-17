package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.*;
import java.util.List;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExperienceRequest {
    private String clinicName;
    private String position;
    private Date startWork;
    private Date endWork;
    private String specialization;
    private List<Long> skillIds;
    private String jobDescription;
}
