package khanhnq.project.clinicbookingmanagementsystem.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CityResponse {
    private Long cityId;
    private String cityName;
}