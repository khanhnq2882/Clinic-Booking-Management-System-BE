package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import khanhnq.project.clinicbookingmanagementsystem.model.response.AddressResponse;
import khanhnq.project.clinicbookingmanagementsystem.model.response.FileResponse;
import lombok.*;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long userId;
    private String userCode;
    private String email;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String phoneNumber;
    private AddressResponse userAddress;
    private FileResponse avatar;
    private Set<String> roleNames;
    private String status;
    private String createdAt;
}
