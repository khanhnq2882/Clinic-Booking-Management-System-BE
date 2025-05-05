package khanhnq.project.clinicbookingmanagementsystem.model.response;

import lombok.*;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileResponse {
    private String fileType;
    private String fileName;
    private String fileUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileResponse that)) return false;
        return Objects.equals(fileType, that.fileType) && Objects.equals(fileName, that.fileName) && Objects.equals(fileUrl, that.fileUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileType, fileName, fileUrl);
    }
}
