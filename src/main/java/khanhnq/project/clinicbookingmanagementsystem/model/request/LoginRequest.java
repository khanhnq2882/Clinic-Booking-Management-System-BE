package khanhnq.project.clinicbookingmanagementsystem.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
    @JsonProperty("isRememberMe")
    private boolean isRememberMe;
}
