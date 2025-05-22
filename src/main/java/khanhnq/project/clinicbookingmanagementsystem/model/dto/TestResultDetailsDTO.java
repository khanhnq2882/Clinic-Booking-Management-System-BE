package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestResultDetailsDTO {
    private String testAttributeName;
    private String testAttributeUnit;
    private String attributeMetadata;
    private String testResult;
    private String testResultNote;
    private String testResultStatus;
    private Double normalMinValue;
    private Double normalMaxValue;
    private Double normalEqualValue;
    private String normalExpectedValue;
    private String normalRangeType;
}
