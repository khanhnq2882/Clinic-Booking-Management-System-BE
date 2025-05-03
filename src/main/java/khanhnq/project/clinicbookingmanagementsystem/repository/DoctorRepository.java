package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query(value = "select * from doctor where user_id = :userId", nativeQuery = true)
    Doctor findDoctorByUserId(Long userId);

    @Query(value = "select * from doctor where specialization_id = :specializationId", nativeQuery = true)
    List<Doctor> getDoctorsBySpecializationId(@Param("specializationId") Long specializationId);
}
