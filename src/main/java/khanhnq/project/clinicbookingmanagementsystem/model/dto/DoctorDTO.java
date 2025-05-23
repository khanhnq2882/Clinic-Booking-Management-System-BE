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
public class DoctorDTO {
    private Long doctorId;
    private String firstName;
    private String lastName;
    private String specializationName;
    private String biography;
    private String educationLevel;
    private FileResponse file;
    private Set<DayOfWeekDTO> daysOfWeek = new HashSet<>();
}
