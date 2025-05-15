package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestResultDTO {
    private Long testPackageAttributeId;
    private String result;
    private String note;
}