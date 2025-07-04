package khanhnq.project.clinicbookingmanagementsystem.model.request;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.TestPackageAttributeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestPackageRequest {
    private String testPackageName;
    private String testPackagePrice;
    private String testPreparationRequirements;
    private String testDescription;
    private Long serviceId;
    private List<TestPackageAttributeDTO> testPackageAttributes;
}