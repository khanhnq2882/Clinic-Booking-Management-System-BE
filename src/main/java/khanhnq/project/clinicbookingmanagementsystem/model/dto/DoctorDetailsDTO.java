package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorDetailsDTO {
    private Long doctorId;
    private String userCode;
    private String firstName;
    private String lastName;
    private String biography;
    private String careerDescription;
    private String educationLevel;
    private String specializationName;
    private Set<WorkExperienceDTO> workExperiences = new HashSet<>();
    private Set<FileResponse> files = new HashSet<>();
    private Set<DayOfWeekDTO> daysOfWeek = new HashSet<>();
}