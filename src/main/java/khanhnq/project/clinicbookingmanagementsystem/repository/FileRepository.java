package khanhnq.project.clinicbookingmanagementsystem.repository;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Query(value = "SELECT f FROM File AS f WHERE f.user.userId = :userId")
    List<File> getFilesById(@Param("userId") Long userId);

    @Query(value = "SELECT f FROM File AS f WHERE f.user.userId = :userId AND f.fileType = :fileType")
    File getFileByType(@Param("userId") Long userId, @Param("fileType") String fileType);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM file WHERE user_id = :userId AND file_type = :fileType", nativeQuery = true)
    void deleteFileByFileType (@Param("userId") Long userId, @Param("fileType") String fileType);
}
