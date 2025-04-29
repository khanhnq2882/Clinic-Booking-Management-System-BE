package khanhnq.project.clinicbookingmanagementsystem.model.dto;

import khanhnq.project.clinicbookingmanagementsystem.model.response.AddressResponse;
import lombok.*;
import java.util.Date;
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
    private Date dateOfBirth;
    private int gender;
    private String phoneNumber;
    private AddressResponse userAddress;
    private Set<String> roleNames;
    private String status;
}
