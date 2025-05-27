package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabResultDetailsDTO {
    private LabResultInfoDTO labResultInfo;
    private Set<TestResultDetailsDTO> testResultsDetails = new HashSet<>();
}
