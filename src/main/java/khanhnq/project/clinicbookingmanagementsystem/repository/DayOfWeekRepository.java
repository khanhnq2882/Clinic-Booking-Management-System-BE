package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.DayOfWeek;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EDayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, Long> {
    @Query(value = "SELECT d FROM DayOfWeek AS d WHERE d.user.userId = :userId AND d.dayOfWeek = :dayOfWeek")
    DayOfWeek getDayOfWeekByDay (@Param("userId") Long userId, @Param("dayOfWeek") EDayOfWeek dayOfWeek);
}
