package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkExperienceDTO {
    private String position;
    private String workSpecializationName;
    private String workPlace;
    private Integer yearOfStartWork;
    private Integer yearOfEndWork;
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkExperienceDTO that)) return false;
        return Objects.equals(position, that.position) &&
                Objects.equals(workSpecializationName, that.workSpecializationName) &&
                Objects.equals(workPlace, that.workPlace) &&
                Objects.equals(yearOfStartWork, that.yearOfStartWork) &&
                Objects.equals(yearOfEndWork, that.yearOfEndWork) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, workSpecializationName, workPlace, yearOfStartWork, yearOfEndWork, description);
    }
}
