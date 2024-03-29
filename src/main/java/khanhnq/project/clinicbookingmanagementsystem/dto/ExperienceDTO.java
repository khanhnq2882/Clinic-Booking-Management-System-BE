package khanhnq.project.clinicbookingmanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExperienceDTO {
    private String position;
    private String specialization;
    private String workPlace;
    private int startWork;
    private int endWork;
}
