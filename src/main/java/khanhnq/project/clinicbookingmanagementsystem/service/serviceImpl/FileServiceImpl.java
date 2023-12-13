package khanhnq.project.clinicbookingmanagementsystem.service.serviceImpl;

import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import khanhnq.project.clinicbookingmanagementsystem.repository.FileRepository;
import khanhnq.project.clinicbookingmanagementsystem.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    public Stream<File> loadFilesByUserId(Long userId) {
        return fileRepository.getFilesById(userId).stream();
    }

    @Override
    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId).get();
    }

}
