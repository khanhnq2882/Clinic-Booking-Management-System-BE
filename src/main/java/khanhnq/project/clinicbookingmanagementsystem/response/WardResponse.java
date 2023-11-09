package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WardResponse {
    private Long wardId;
    private String wardName;
    private Long districtId;
}
