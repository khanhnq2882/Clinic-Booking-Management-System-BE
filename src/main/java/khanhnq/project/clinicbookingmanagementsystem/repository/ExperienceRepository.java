package khanhnq.project.clinicbookingmanagementsystem.repository;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    @Query(value = "SELECT e FROM Experience AS e WHERE e.user.userId = :userId")
    List<Experience> getExperiencesByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM experiences_skills WHERE experience_id = :experienceId", nativeQuery = true)
    void deleteExperiencesSkills(@Param("experienceId") Long experienceId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM experience WHERE experience_id = :experienceId", nativeQuery = true)
    void deleteExperiences(@Param("experienceId") Long experienceId);

}