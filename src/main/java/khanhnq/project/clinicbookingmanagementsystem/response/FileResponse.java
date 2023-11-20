package khanhnq.project.clinicbookingmanagementsystem.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileResponse {
    private String fileType;
    private String fileName;
    private String fileUrl;
}
