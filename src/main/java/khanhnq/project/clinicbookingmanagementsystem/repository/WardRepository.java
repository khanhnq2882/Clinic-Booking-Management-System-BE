package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    @Query(value = "SELECT w FROM Ward AS w INNER JOIN District AS d ON w.district.districtId = d.districtId WHERE w.district.districtId = :districtId")
    List<Ward> getWardsByDistrictId(@Param("districtId") Long districtId);

    @Query(value = "SELECT * FROM ward WHERE ward_name COLLATE utf8mb4_unicode_ci LIKE %:wardName%", nativeQuery = true)
    List<Ward> getWardsByWardName (String wardName);
}