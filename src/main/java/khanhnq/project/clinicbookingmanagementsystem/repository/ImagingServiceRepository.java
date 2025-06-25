package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.ImagingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ImagingServiceRepository extends JpaRepository<ImagingService, Long> {

    @Query(value = "SELECT imaging_service_code FROM imaging_service WHERE imaging_service_type = :type", nativeQuery = true)
    List<String> getImagingServicesByType (@Param("type") String type);
}