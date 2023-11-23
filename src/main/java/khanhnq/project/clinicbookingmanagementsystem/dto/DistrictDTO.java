package khanhnq.project.clinicbookingmanagementsystem.dto;

import khanhnq.project.clinicbookingmanagementsystem.entity.Ward;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictDTO {
    private String name;
    private Long code;
    private List<WardDTO> wards = new ArrayList<>();
}