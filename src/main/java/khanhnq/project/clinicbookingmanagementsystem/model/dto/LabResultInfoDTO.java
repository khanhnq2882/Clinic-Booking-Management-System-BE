package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabResultInfoDTO {
    private Long labResultId;
    private String testPackageName;
    private LocalDateTime sampleCollectionTime;
    private LocalDateTime sampleReceptionTime;
    private LocalDateTime testDate;
    private LocalDateTime resultDeliveryDate;
    private String labNote;
    private String labStatus;
    private String educationLevel;
    private String doctorFirstName;
    private String doctorLastName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabResultInfoDTO that)) return false;
        return Objects.equals(labResultId, that.labResultId) &&
                Objects.equals(testPackageName, that.testPackageName) &&
                Objects.equals(sampleCollectionTime, that.sampleCollectionTime) &&
                Objects.equals(sampleReceptionTime, that.sampleReceptionTime) &&
                Objects.equals(testDate, that.testDate) &&
                Objects.equals(resultDeliveryDate, that.resultDeliveryDate) &&
                Objects.equals(labNote, that.labNote) &&
                Objects.equals(labStatus, that.labStatus) &&
                Objects.equals(educationLevel, that.educationLevel) &&
                Objects.equals(doctorFirstName, that.doctorFirstName) &&
                Objects.equals(doctorLastName, that.doctorLastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labResultId, testPackageName, sampleCollectionTime,
                sampleReceptionTime, testDate, resultDeliveryDate, labNote,
                labStatus, educationLevel, doctorFirstName, doctorLastName);
    }
}
