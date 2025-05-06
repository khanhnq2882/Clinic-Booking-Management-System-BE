package khanhnq.project.clinicbookingmanagementsystem.model.response;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDetailsDTO;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponse {
    private long totalItems;
    private List<DoctorDetailsDTO> doctors;
    private long totalPages;
    private long currentPage;
}
