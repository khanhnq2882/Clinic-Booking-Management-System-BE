package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.WorkSchedule;
import khanhnq.project.clinicbookingmanagementsystem.entity.enums.EDayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
//    @Query(value = "SELECT ws FROM WorkSchedule AS ws WHERE ws.user.userId = :userId")
//    List<WorkSchedule> getWorkSchedulesByUserId(@Param("userId") Long userId);

//    @Query(value = "SELECT ws FROM WorkSchedule AS ws " +
//            "INNER JOIN ws.user AS u " +
//            "INNER JOIN u.specialization AS s " +
//            "WHERE s.specializationName = :specializationName " +
//            "AND ws.startTime = :startTime " +
//            "AND ws.endTime = :endTime")
//    WorkSchedule getWorkScheduleByTime (@Param("specializationName") String specializationName, @Param("startTime")LocalTime startTime, @Param("endTime")LocalTime endTime);

    @Query(value = "SELECT ws FROM WorkSchedule AS ws " +
            "INNER JOIN ws.dayOfWeek AS d " +
            "INNER JOIN d.user AS u " +
            "WHERE u.specialization.specializationId = :specializationId " +
            "AND d.dayOfWeek = :dayOfWeek")
    List<WorkSchedule> getWorkSchedulesByDay (@Param("specializationId") Long specializationId, @Param("dayOfWeek") EDayOfWeek dayOfWeek);
}
