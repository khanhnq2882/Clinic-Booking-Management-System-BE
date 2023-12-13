package khanhnq.project.clinicbookingmanagementsystem.repository;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.entity.Booking;
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
    @Query(value = "SELECT b FROM Booking AS b WHERE b.workSchedule.workScheduleId = :workScheduleId")
    List<Booking> getAllUserBookings(@Param("workScheduleId") Long workScheduleId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE booking SET status = 'CONFIRMED' WHERE booking_id = :bookingId", nativeQuery = true)
    void confirmedBooking (@Param("bookingId") Long bookingId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE booking SET status = 'CANCELLED' WHERE booking_id = :bookingId", nativeQuery = true)
    void cancelledBooking (@Param("bookingId") Long bookingId);

    @Query(value = "SELECT b FROM Booking AS b " +
            "INNER JOIN FETCH b.workSchedule AS ws " +
            "INNER JOIN FETCH ws.user AS u " +
            "WHERE u.userId = :userId")
    Page<Booking> getAllBookings(@Param("userId") Long userId, Pageable pageable);
}
