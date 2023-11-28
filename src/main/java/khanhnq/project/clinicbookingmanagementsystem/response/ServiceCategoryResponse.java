package khanhnq.project.clinicbookingmanagementsystem.response;

import khanhnq.project.clinicbookingmanagementsystem.dto.ServiceCategoryDTO;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCategoryResponse {
    private long totalItems;
    private List<ServiceCategoryDTO> serviceCategories;
    private long totalPages;
    private long currentPage;
}
