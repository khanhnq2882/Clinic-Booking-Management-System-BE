package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    @Query(value = "SELECT ws FROM WorkSchedule AS ws WHERE ws.user.userId = :userId")
    Set<WorkSchedule> getWorkSchedulesByUserId(@Param("userId") Long userId);
}
