package khanhnq.project.clinicbookingmanagementsystem.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalImageRequest {
    private String conclusion;
    private String description;
    private LocalDateTime performedAt;
    private Long doctorPrescribedId;
    private Long imagingServiceId;
    private Long medicalRecordId;
}