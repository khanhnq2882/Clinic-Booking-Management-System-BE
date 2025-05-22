package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.TestPackageAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TestPackageAttributeRepository extends JpaRepository<TestPackageAttribute, Long> {
    @Query(value = "SELECT tpa.test_package_attribute_id FROM test_package_attribute AS tpa " +
            "INNER JOIN test_package_attribute_mapping AS tpam " +
            "ON tpa.test_package_attribute_id = tpam.test_package_attribute_id " +
            "WHERE tpam.test_package_id = :testPackageId", nativeQuery = true)
    List<Long> getTestPackageAttributeIdsByTestPackageId(@Param("testPackageId") Long testPackageId);
}
