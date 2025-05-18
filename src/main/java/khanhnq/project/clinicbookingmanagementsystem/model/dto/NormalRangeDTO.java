package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NormalRangeDTO {
    private String gender;
    private Integer ageMin;
    private Integer ageMax;
    private String normalRangeType;
    private Double minValue;
    private Double maxValue;
    private String expectedValue;
    private String normalText;
}
