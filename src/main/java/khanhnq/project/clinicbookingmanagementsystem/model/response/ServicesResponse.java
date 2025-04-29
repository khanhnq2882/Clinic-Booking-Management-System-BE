package khanhnq.project.clinicbookingmanagementsystem.model.response;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.ServicesDTO;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServicesResponse {
    private long totalItems;
    private List<ServicesDTO> services;
    private long totalPages;
    private long currentPage;
}
