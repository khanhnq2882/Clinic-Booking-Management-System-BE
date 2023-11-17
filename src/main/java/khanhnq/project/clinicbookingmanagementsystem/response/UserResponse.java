package khanhnq.project.clinicbookingmanagementsystem.response;

import khanhnq.project.clinicbookingmanagementsystem.entity.Address;
import lombok.*;

import java.util.List;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String userCode;
    private String email;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private int gender;
    private String phoneNumber;
    private String specificAddress;
    private String wardName;
    private String districtName;
    private String cityName;
    private List<String> roles;
    private String status;
}
