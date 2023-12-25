package khanhnq.project.clinicbookingmanagementsystem.request;

import jakarta.validation.constraints.NotNull;
import khanhnq.project.clinicbookingmanagementsystem.annotation.EmailConstraint;
import khanhnq.project.clinicbookingmanagementsystem.annotation.PasswordConstraint;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotNull(message = "User name can't be empty.")
    @Length(max = 20, message = "User name has a maximum of 20 characters")
    private String username;

    @EmailConstraint
    private String email;

    @PasswordConstraint
    private String password;

    private Set<String> roles = new HashSet<>();
}
