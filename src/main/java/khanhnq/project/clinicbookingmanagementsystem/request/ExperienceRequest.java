package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.*;
import java.util.List;
import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExperienceRequest {
    private String clinicName;
    private String position;
    private Date startWork;
    private Date endWork;
    private Long specializationId;
    private List<Long> skillIds;
    private String jobDescription;
}
