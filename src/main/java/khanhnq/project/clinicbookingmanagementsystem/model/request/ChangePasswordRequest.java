package khanhnq.project.clinicbookingmanagementsystem.model.request;

import khanhnq.project.clinicbookingmanagementsystem.common.annotation.PasswordConstraint;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {
    private String currentPassword;

    @PasswordConstraint
    private String newPassword;

    private String confirmPassword;
}
