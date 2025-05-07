package khanhnq.project.clinicbookingmanagementsystem.repository;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.entity.Booking;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.BookingInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT b FROM Booking AS b INNER JOIN b.workSchedule AS ws INNER JOIN ws.daysOfWeek AS d INNER JOIN d.doctor AS dt WHERE dt.doctorId = :doctorId")
    List<Booking> getBookingsByDoctor (@Param("doctorId") Long doctorId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE booking SET status = 'CONFIRMED' WHERE booking_id = :bookingId", nativeQuery = true)
    void confirmedBooking (@Param("bookingId") Long bookingId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE booking SET status = 'CANCELLED' WHERE booking_id = :bookingId", nativeQuery = true)
    void cancelledBooking (@Param("bookingId") Long bookingId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE booking SET status = 'COMPLETED' WHERE booking_id = :bookingId", nativeQuery = true)
    void completedBooking (@Param("bookingId") Long bookingId);

    @Query(value = "SELECT b FROM Booking AS b " +
            "INNER JOIN b.workSchedule AS ws " +
            "INNER JOIN ws.daysOfWeek AS d " +
            "INNER JOIN d.doctor AS dt " +
            "WHERE dt.doctorId = :doctorId")
    Page<Booking> getAllBookings(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query(value = "SELECT b FROM Booking AS b WHERE b.user.userId IS NULL")
    Page<Booking> getBookingsWithNullUser (Pageable pageable);

    @Query(value = "SELECT b FROM Booking AS b WHERE b.user.userId = :userId")
    Page<Booking> getBookingsWithUserId (@Param("userId") Long userId, Pageable pageable);

    @Query(value = "select b.booking_id as bookingId, dof.working_day as workingDay, ws.start_time as startTime, ws.end_time as endTime \n" +
            "from booking as b\n" +
            "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
            "inner join day_of_week as dof on ws.day_of_week_id = dof.day_of_week_id\n" +
            "where b.work_schedule_id = :workScheduleId and b.status not in ('CANCELLED')", nativeQuery = true)
    List<BookingInfo> getBookingsByWorkScheduleId(@Param("workScheduleId") Long workScheduleId);

    @Query(value = "select b.booking_id as bookingId, b.created_at as createdAt, dof.working_day as workingDay, " +
            "ws.start_time as startTime, ws.end_time as endTime \n" +
            "from booking as b\n" +
            "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
            "inner join day_of_week as dof on ws.day_of_week_id = dof.day_of_week_id\n" +
            "where b.booking_id = :bookingId", nativeQuery = true)
    BookingInfo getBookingInfoByBookingId(@Param("bookingId") Long bookingId);

}
