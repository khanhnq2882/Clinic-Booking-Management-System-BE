package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Set<String> roles = new HashSet<>();
}
