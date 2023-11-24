package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPageResponse {
    private int totalItems;
    private List<UserResponse> users;
    private int totalPages;
    private int currentPage;
}
