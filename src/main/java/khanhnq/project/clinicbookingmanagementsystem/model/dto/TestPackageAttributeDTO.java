package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestPackageAttributeDTO {
    private String name;
    private String unit;
    private List<NormalRangeDTO> normalRanges = new ArrayList<>();
    private Map<String, String> attributeMetadata;
}
