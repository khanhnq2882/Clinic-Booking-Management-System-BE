package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.DaysOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;

@Repository
public interface DayOfWeekRepository extends JpaRepository<DaysOfWeek, Long> {
    @Query(value = "SELECT d FROM DaysOfWeek AS d WHERE d.user.userId = :userId AND d.dayOfWeek = :dayOfWeek")
    DaysOfWeek getDayOfWeekByDay (@Param("userId") Long userId, @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
