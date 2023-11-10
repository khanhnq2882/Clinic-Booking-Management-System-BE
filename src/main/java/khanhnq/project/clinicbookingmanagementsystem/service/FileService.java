package khanhnq.project.clinicbookingmanagementsystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    void init();
    void saveAvatar(MultipartFile file);


}
