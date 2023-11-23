package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest {
    private Long serviceCategoryId;
    private String serviceName;
    private Double price;
    private String description;
}
