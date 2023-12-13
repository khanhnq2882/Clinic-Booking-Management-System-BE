package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestDoctorPageResponse {
    private long totalItems;
    private List<RequestDoctorResponse> requestDoctorResponses;
    private long totalPages;
    private long currentPage;
}
