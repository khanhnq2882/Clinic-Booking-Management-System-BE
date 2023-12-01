package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import org.springframework.core.io.Resource;
import java.util.stream.Stream;

public interface FileService {
    Stream<File> loadFilesByUserId(Long userId);
    File getFileById(Long fileId);

}
