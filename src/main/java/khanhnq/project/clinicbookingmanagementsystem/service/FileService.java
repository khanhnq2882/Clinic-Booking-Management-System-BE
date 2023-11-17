package khanhnq.project.clinicbookingmanagementsystem.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileService {
    void init();
    void save(MultipartFile file);
    Stream<Path> loadFiles();
    Resource load(String filename);

}
