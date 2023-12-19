package khanhnq.project.clinicbookingmanagementsystem.request;

import khanhnq.project.clinicbookingmanagementsystem.annotation.PasswordConstraint;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {
    private String currentPassword;

    @PasswordConstraint
    private String newPassword;

    @PasswordConstraint
    private String confirmPassword;
}
