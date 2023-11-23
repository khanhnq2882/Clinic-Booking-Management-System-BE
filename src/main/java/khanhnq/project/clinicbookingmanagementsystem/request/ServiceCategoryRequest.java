package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCategoryRequest {
    private Long specializationId;
//    private Set<ServiceCategoryDTO> serviceCategories;
    private String serviceCategoryName;
    private String description;
}
