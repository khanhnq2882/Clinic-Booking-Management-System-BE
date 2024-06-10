package khanhnq.project.clinicbookingmanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServicesDTO {
    private Long serviceId;
    private String serviceName;
    private Double price;
    private String description;
    private String status;
    private String serviceCategoryName;
}
