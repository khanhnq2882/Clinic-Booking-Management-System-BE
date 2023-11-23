package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicesRepository extends JpaRepository<Services, Long> {
    @Query(value = "SELECT s FROM Services AS s WHERE s.serviceCode LIKE %:serviceCode%")
    List<Services> getServicesByCode(@Param("serviceCode") String serviceCode);
}
