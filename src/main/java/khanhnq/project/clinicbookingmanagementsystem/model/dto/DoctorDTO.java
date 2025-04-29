package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import khanhnq.project.clinicbookingmanagementsystem.model.response.AddressResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
import lombok.*;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorDTO {
    private Long userId;
    private String userCode;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressResponse doctorAddress;
    private String specializationName;
    private Set<ExperienceDTO> experiences;
    private String status;
    private List<FileResponse> files;
}