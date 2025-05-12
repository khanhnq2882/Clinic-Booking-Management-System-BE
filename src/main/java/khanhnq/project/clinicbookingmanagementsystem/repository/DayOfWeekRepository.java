package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.DaysOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;

@Repository
public interface DayOfWeekRepository extends JpaRepository<DaysOfWeek, Long> {
    @Query(value = "SELECT d FROM DaysOfWeek AS d WHERE d.doctor.doctorId = :doctorId AND d.workingDay = :workingDay")
    DaysOfWeek getDayOfWeekByWorkingDay(@Param("doctorId") Long doctorId, @Param("workingDay") Date workingDay);
}
