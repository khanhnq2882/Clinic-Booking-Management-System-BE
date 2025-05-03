package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkExperienceDTO {
    private String position;
    private String workSpecializationName;
    private String workPlace;
    private int yearOfStartWork;
    private int yearOfEndWork;
    private String description;
}
