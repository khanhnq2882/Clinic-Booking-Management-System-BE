package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    @Query("SELECT ws FROM WorkSchedule ws " +
            "JOIN ws.daysOfWeek dow " +
            "JOIN dow.doctor d " +
            "WHERE d.doctorId = :doctorId AND dow.workingDay = :workingDay")
    List<WorkSchedule> getWorkSchedulesByWorkingDay(@Param("doctorId") Long doctorId, @Param("workingDay") Date workingDay);

    @Query("""
            SELECT ws FROM WorkSchedule ws
            JOIN ws.daysOfWeek dow
            JOIN dow.doctor dt
            WHERE dt.doctorId = :doctorId
            AND dow.workingDay = :workingDay
            AND NOT EXISTS (
                SELECT 1 FROM Booking b
                WHERE b.workSchedule = ws
            )
          """)
    List<WorkSchedule> getWorkSchedulesNotInBooking(@Param("doctorId") Long doctorId, @Param("workingDay") Date workingDay);

    @Query("""
            SELECT ws FROM WorkSchedule ws
            JOIN ws.daysOfWeek dow
            JOIN dow.doctor dt
            WHERE dt.doctorId = :doctorId
            AND dow.workingDay = :workingDay
            AND EXISTS (
                SELECT 1 FROM Booking b
                WHERE b.workSchedule = ws
            )
          """)
    List<WorkSchedule> getWorkSchedulesInBooking(@Param("doctorId") Long doctorId, @Param("workingDay") Date workingDay);
}
