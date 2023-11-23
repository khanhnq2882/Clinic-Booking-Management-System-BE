package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCategoryResponse {
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private Long specializationId;
}
