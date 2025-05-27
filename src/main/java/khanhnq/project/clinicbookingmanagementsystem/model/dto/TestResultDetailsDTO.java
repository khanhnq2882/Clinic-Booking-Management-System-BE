package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestResultDetailsDTO that)) return false;
        return Objects.equals(testAttributeName, that.testAttributeName) &&
                Objects.equals(testAttributeUnit, that.testAttributeUnit) &&
                Objects.equals(attributeMetadata, that.attributeMetadata) &&
                Objects.equals(testResult, that.testResult) &&
                Objects.equals(testResultNote, that.testResultNote) &&
                Objects.equals(testResultStatus, that.testResultStatus) &&
                Objects.equals(normalMinValue, that.normalMinValue) &&
                Objects.equals(normalMaxValue, that.normalMaxValue) &&
                Objects.equals(normalEqualValue, that.normalEqualValue) &&
                Objects.equals(normalExpectedValue, that.normalExpectedValue) &&
                Objects.equals(normalRangeType, that.normalRangeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testAttributeName, testAttributeUnit, attributeMetadata,
                testResult, testResultNote, testResultStatus, normalMinValue,
                normalMaxValue, normalEqualValue, normalExpectedValue, normalRangeType);
    }
}
