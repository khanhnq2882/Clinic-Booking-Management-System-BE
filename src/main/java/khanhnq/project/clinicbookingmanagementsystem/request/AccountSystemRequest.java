package khanhnq.project.clinicbookingmanagementsystem.request;

import jakarta.validation.constraints.NotNull;
import khanhnq.project.clinicbookingmanagementsystem.annotation.EmailConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import java.util.Set;

@Data
@AllArgsConstructor
public class AccountSystemRequest {
    @EmailConstraint
    private String email;

    @NotNull(message = "User name can't be empty.")
    @Length(min = 5, max = 20, message = "Username must have a minimum of 5 characters and a maximum of 20 characters")
    private String username;

    private Set<String> roles;
}
