package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = "SELECT b FROM Booking AS b WHERE b.workSchedule.workScheduleId = :workScheduleId")
    List<Booking> getAllUserBookings(@Param("workScheduleId") Long workScheduleId);

    @Query(value = "UPDATE booking SET status = :status WHERE booking_id = :bookingId", nativeQuery = true)
    void changeBookingStatus(@Param("bookingId") Long bookingId, @Param("status") String status);
}
