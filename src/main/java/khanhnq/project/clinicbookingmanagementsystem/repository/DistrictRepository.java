package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    @Query(value = "SELECT d FROM District AS d INNER JOIN City AS c ON d.city.cityId = c.cityId WHERE d.city.cityId = :cityId")
    List<District> getDistrictsByCityId(@Param("cityId") Long cityId);

    @Query(value = "SELECT d FROM District AS d WHERE d.districtName = :districtName")
    List<District> getDistrictsByDistrictName (String districtName);

}