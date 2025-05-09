package khanhnq.project.clinicbookingmanagementsystem.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenResponse {
    private String refreshToken;
    private String accessToken;
    private String tokenType;
}
