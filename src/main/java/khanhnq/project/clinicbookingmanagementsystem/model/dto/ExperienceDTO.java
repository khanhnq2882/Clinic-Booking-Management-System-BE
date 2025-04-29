package khanhnq.project.clinicbookingmanagementsystem.model.dto;

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
    private int yearOfStartWork;
    private int yearOfEndWork;
}
