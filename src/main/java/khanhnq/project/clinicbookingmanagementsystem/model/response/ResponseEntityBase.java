package khanhnq.project.clinicbookingmanagementsystem.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseEntityBase {
    private int statusCode;
    private String errorMessage;
    private Object data;
}