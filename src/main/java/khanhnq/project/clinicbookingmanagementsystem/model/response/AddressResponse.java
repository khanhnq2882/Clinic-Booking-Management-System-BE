package khanhnq.project.clinicbookingmanagementsystem.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {
    private Long addressId;
    private String specificAddress;
    private String wardName;
    private String districtName;
    private String cityName;
}
