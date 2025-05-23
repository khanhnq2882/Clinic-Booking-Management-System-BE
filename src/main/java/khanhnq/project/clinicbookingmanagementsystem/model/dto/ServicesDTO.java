package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServicesDTO {
    private Long serviceId;
    private String serviceName;
    private Double servicePrice;
    private String description;
    private String status;
    private String specializationName;
}
