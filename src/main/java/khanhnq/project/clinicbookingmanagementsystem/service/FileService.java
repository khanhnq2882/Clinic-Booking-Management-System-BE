package khanhnq.project.clinicbookingmanagementsystem.service;

import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import org.springframework.core.io.Resource;
import java.util.stream.Stream;

public interface FileService {
    void init();
    Stream<File> loadFilesByUserId(Long userId);
    Resource load(String filename);
    File getFileById(Long fileId);

}
