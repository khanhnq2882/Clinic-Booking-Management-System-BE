package khanhnq.project.clinicbookingmanagementsystem.dto;

import khanhnq.project.clinicbookingmanagementsystem.entity.District;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityDTO {
    private String name;
    private Long code;
    private List<DistrictDTO> districts = new ArrayList<>();
}