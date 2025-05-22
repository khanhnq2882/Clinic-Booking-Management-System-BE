package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EGender;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.ENormalRangeType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultDTO {
    private String result;
    private int yearOld;
    private ENormalRangeType normalRangeType;
    private Integer ageMin;
    private Integer ageMax;
    private Double minValue;
    private Double maxValue;
    private Double equalValue;
    private String expectedValue;
    private EGender normalGender;
}
