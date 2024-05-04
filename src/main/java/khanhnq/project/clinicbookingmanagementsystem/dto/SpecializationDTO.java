package khanhnq.project.clinicbookingmanagementsystem.dto;

import lombok.*;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecializationDTO {
    private Long specializationId;
    private String specializationName;

    @Override
    public boolean equals(Object o) {
        if (Objects.isNull(o)) return false;
        if (o == this) return true;
        SpecializationDTO specialization = (SpecializationDTO) o;
        return Objects.equals(this.getSpecializationId(), specialization.specializationId)
                && this.specializationName.equalsIgnoreCase(specialization.getSpecializationName());
    }
}
