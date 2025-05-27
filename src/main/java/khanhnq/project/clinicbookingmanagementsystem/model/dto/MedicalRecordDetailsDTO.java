package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalRecordDetailsDTO {
    private MedicalRecordInfoDTO medicalRecordInfo;
    private List<LabResultDetailsDTO> labResultsDetails = new ArrayList<>();
}
