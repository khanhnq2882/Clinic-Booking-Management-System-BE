package khanhnq.project.clinicbookingmanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCategoryDTO {
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private Long specializationId;
    private String specializationName;
    private String description;
}
