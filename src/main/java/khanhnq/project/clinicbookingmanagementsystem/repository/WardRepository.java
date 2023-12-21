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

    @Query(value = "SELECT w FROM Ward AS w WHERE w.wardName LIKE %:wardName%")
    List<Ward> getWardsByWardName (String wardName);
}