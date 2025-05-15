package khanhnq.project.clinicbookingmanagementsystem.model.request;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.TestResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabResultRequest {
    private LocalDateTime sampleCollectionTime;
    private LocalDateTime sampleReceptionTime;
    private LocalDateTime testDate;
    private LocalDateTime resultDeliveryDate;
    private String note;
    private Long medicalRecordId;
    private Long doctorPrescribedId;
    private Long testPackageId;
    private List<TestResultDTO> testResults = new ArrayList<>();
}
