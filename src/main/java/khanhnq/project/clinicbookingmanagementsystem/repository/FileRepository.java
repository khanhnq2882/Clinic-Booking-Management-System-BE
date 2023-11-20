package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Query(value = "SELECT f FROM File AS f WHERE f.user.userId = :userId")
    List<File> getFilesById(@Param("userId") Long userId);
}
