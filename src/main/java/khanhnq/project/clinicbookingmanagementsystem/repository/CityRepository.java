package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    @Query(value = "SELECT * FROM city WHERE city_name COLLATE utf8mb4_unicode_ci LIKE %:cityName%", nativeQuery = true)
    List<City> getCitiesByCityName (String cityName);
}