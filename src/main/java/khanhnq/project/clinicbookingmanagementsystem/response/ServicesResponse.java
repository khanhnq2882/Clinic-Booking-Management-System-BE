package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServicesResponse {
    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private Double price;
    private String description;
    private String status;
    private String serviceCategoryName;
}
