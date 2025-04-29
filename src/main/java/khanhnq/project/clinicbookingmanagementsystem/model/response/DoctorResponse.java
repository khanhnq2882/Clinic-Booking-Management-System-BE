package khanhnq.project.clinicbookingmanagementsystem.model.response;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorDTO;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponse {
    private long totalItems;
    private List<DoctorDTO> doctors;
    private long totalPages;
    private long currentPage;
}
