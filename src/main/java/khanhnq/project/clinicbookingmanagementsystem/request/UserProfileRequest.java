package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private int gender;
    private String phoneNumber;
    private String specificAddress;
    private Long wardId;
}