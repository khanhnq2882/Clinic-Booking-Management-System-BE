package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorInfoDTO {
    private Long doctorId;
    private String userCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String educationLevel;
    private String specializationName;
    private String status;
    private FileResponse avatar;
    private String createdAt;
}
