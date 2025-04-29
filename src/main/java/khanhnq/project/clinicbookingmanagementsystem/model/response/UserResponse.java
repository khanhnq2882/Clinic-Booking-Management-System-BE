package khanhnq.project.clinicbookingmanagementsystem.model.response;

import khanhnq.project.clinicbookingmanagementsystem.model.dto.UserDTO;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private long totalItems;
    private List<UserDTO> users;
    private long totalPages;
    private long currentPage;
}
