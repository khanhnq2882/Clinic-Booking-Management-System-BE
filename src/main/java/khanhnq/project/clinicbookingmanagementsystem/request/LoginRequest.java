package khanhnq.project.clinicbookingmanagementsystem.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotNull(message = "User name can't be empty.")
    @Length(max = 20, message = "User name has a maximum of 20 characters")
    private String username;

    private String password;
}
