package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    @Query(value = "SELECT sc FROM ServiceCategory AS sc " +
            "INNER JOIN Specialization AS s " +
            "ON sc.specialization.specializationId = s.specializationId " +
            "WHERE sc.specialization.specializationId = :specializationId")
    List<ServiceCategory> getServiceCategoriesBySpecializationId(@Param("specializationId") Long specializationId);
    ServiceCategory getServiceCategoryByServiceCategoryName(String serviceCategoryName);
}
