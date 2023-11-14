package khanhnq.project.clinicbookingmanagementsystem.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileRequest {
    private MultipartFile medicalDegree;
    private MultipartFile medicalLicense;
}
