package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPageResponse {
    private long totalItems;
    private List<UserResponse> users;
    private long totalPages;
    private long currentPage;
}
