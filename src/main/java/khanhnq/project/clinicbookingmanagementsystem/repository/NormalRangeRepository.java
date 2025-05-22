package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.NormalRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NormalRangeRepository extends JpaRepository<NormalRange, Long> {
    @Modifying
    @Query(value = "DELETE FROM normal_range WHERE test_package_attribute_id = :testPackageAttributeId", nativeQuery = true)
    void deleteNormalRangesByTestPackageAttributeId(@Param("testPackageAttributeId") Long testPackageAttributeId);
}
