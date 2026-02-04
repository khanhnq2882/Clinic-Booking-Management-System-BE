package khanhnq.project.clinicbookingmanagementsystem.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImagingServiceRequest {
    private String imagingServiceName;
    private BigDecimal imagingServicePrice;
    private String description;
    private Integer estimatedMinutes;
    private String preparationRequirements;
    private String imagingServiceType;
    private Long serviceId;
}