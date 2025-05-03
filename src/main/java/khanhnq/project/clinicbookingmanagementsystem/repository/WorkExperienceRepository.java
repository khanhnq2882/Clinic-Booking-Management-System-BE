package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience,Long> {
    @Query(value = "select * from work_experience where doctor_id = :doctorId", nativeQuery = true)
    List<WorkExperience> findWorkExperienceByDoctorId(Long doctorId);
}
