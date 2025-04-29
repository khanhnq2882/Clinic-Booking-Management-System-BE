package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityDTO {
    private String name;
    private Long code;
    private List<DistrictDTO> districts = new ArrayList<>();
}