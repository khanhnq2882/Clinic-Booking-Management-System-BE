package khanhnq.project.clinicbookingmanagementsystem.model.request;

import jakarta.validation.constraints.NotNull;
import khanhnq.project.clinicbookingmanagementsystem.annotation.PhoneNumberConstraint;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    @NotNull(message = "First name can't be empty.")
    @Length(max = 20, message = "First name has a maximum of 20 characters.")
    private String firstName;

    @NotNull(message = "Last name can't be empty.")
    @Length(max = 20, message = "Last name has a maximum of 20 characters.")
    private String lastName;

    private Date dateOfBirth;
    private int gender;

    @PhoneNumberConstraint
    private String phoneNumber;

    @NotNull(message = "Specific address can't be empty.")
    @Length(max = 100, message = "Specific address has a maximum of 100 characters.")
    private String specificAddress;

    private Long wardId;
}