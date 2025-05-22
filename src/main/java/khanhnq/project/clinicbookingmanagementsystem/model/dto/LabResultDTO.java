package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabResultDTO {
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
    private List<TestResultDetailsDTO> testResults = new ArrayList<>();
}
