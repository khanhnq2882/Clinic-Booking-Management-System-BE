package khanhnq.project.clinicbookingmanagementsystem.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistrictResponse {
    private Long districtId;
    private String districtName;
    private Long cityId;
}
