package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.DaysOfWeek;
import khanhnq.project.clinicbookingmanagementsystem.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    @Query(value = "SELECT ws FROM WorkSchedule AS ws " +
            "INNER JOIN ws.daysOfWeek AS d " +
            "WHERE d.user.userId = :userId")
    List<WorkSchedule> getWorkSchedulesByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT ws FROM WorkSchedule AS ws WHERE ws.daysOfWeek = :dayOfWeek")
    List<WorkSchedule> getWorkSchedulesByDayOfWeek (@Param("dayOfWeek") DaysOfWeek dayOfWeek);

    @Query(value = "SELECT ws FROM WorkSchedule AS ws " +
            "INNER JOIN ws.daysOfWeek AS d " +
            "INNER JOIN d.user AS u " +
            "INNER JOIN u.specialization AS s " +
            "WHERE s.specializationName = :specializationName " +
            "AND d.dayOfWeek = :dayOfWeek " +
            "AND ws.startTime = :startTime " +
            "AND ws.endTime = :endTime")
    WorkSchedule getWorkScheduleByTime (@Param("specializationName") String specializationName, @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                        @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
