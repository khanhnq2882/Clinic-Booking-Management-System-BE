package khanhnq.project.clinicbookingmanagementsystem.repository;

import jakarta.transaction.Transactional;
import khanhnq.project.clinicbookingmanagementsystem.entity.Booking;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.BookingTimeInfoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = "SELECT \n" +
            "    b.booking_id AS bookingId,\n" +
            "    dof.working_day AS workingDay,\n" +
            "    ws.start_time AS startTime,\n" +
            "    ws.end_time AS endTime\n" +
            "FROM booking AS b\n" +
            "INNER JOIN work_schedule AS ws \n" +
            "    ON b.work_schedule_id = ws.work_schedule_id\n" +
            "INNER JOIN day_of_week AS dof \n" +
            "    ON ws.day_of_week_id = dof.day_of_week_id\n" +
            "WHERE b.work_schedule_id = :workScheduleId\n" +
            "  AND b.status NOT IN ('CANCELLED')", nativeQuery = true)
    List<BookingTimeInfoProjection> getBookingsByWorkScheduleId(@Param("workScheduleId") Long workScheduleId);

    @Query(value = "SELECT \n" +
            "    b.booking_id AS bookingId,\n" +
            "    b.created_at AS createdAt,\n" +
            "    dof.working_day AS workingDay,\n" +
            "    ws.start_time AS startTime,\n" +
            "    ws.end_time AS endTime\n" +
            "FROM booking AS b\n" +
            "INNER JOIN work_schedule AS ws \n" +
            "    ON b.work_schedule_id = ws.work_schedule_id\n" +
            "INNER JOIN day_of_week AS dof \n" +
            "    ON ws.day_of_week_id = dof.day_of_week_id\n" +
            "WHERE b.booking_id = :bookingId", nativeQuery = true)
    BookingTimeInfoProjection getBookingInfoByBookingId(@Param("bookingId") Long bookingId);

    @Modifying
    @Transactional
    @Query(value = "DELETE \n" +
            "FROM booking\n" +
            "WHERE user_id IS NULL\n" +
            "  AND status NOT IN ('CONFIRMED')\n" +
            "  AND created_at < CURRENT_TIMESTAMP() - INTERVAL :hours HOUR", nativeQuery = true)
    void deleteUnconfirmedBookingAfter24Hours(@Param("hours") int hours);

    @Modifying
    @Transactional
    @Query(value = "DELETE \n" +
            "FROM booking\n" +
            "WHERE user_id IS NULL\n" +
            "  AND created_at < CURRENT_TIMESTAMP() - INTERVAL :days DAY", nativeQuery = true)
    void deleteOlderThanDays(@Param("days") int days);

    @Query(
            value = "SELECT \n" +
                    "    b.booking_id AS bookingId,\n" +
                    "    b.booking_code AS bookingCode,\n" +
                    "    s.specialization_name AS specializationName,\n" +
                    "    b.first_name AS firstName,\n" +
                    "    b.last_name AS lastName,\n" +
                    "    DATE_FORMAT(b.date_of_birth, '%d-%m-%Y') AS dateOfBirth,\n" +
                    "    CASE \n" +
                    "        WHEN b.gender = 1 THEN 'Male'\n" +
                    "        WHEN b.gender = 0 THEN 'Female'\n" +
                    "        ELSE 'Other'\n" +
                    "    END AS gender,\n" +
                    "    b.phone_number AS phoneNumber,\n" +
                    "    CONCAT(a.specific_address, ', ', w.ward_name, ', ', d.district_name, ', ', c.city_name) AS userAddress,\n" +
                    "    b.describe_symptoms AS describeSymptoms,\n" +
                    "    DATE_FORMAT(dow.working_day, '%d-%m-%Y') AS workingDay,\n" +
                    "    TIME_FORMAT(ws.start_time, '%H:%i') AS startTime,\n" +
                    "    TIME_FORMAT(ws.end_time, '%H:%i') AS endTime,\n" +
                    "    b.status AS status,\n" +
                    "    DATE_FORMAT(b.created_at, '%d-%m-%Y %H:%i:%s') AS createdAt\n" +
                    "FROM booking AS b\n" +
                    "INNER JOIN address AS a ON b.address_id = a.address_id\n" +
                    "INNER JOIN ward AS w ON a.ward_id = w.ward_id\n" +
                    "INNER JOIN district AS d ON w.district_id = d.district_id\n" +
                    "INNER JOIN city AS c ON d.city_id = c.city_id\n" +
                    "INNER JOIN work_schedule AS ws ON b.work_schedule_id = ws.work_schedule_id\n" +
                    "INNER JOIN day_of_week AS dow ON ws.day_of_week_id = dow.day_of_week_id\n" +
                    "INNER JOIN doctor AS dt ON dow.doctor_id = dt.doctor_id\n" +
                    "INNER JOIN user AS u ON dt.user_id = u.user_id\n" +
                    "INNER JOIN specialization AS s ON dt.specialization_id = s.specialization_id\n" +
                    "WHERE b.user_id = :userId\n" +
                    "ORDER BY b.created_at DESC\n",
            countQuery = "SELECT COUNT(*) FROM booking WHERE user_id = :userId",
            nativeQuery = true
    )
    Page<BookingDetailsInfoProjection> getBookingDetailsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            value = "SELECT \n" +
                    "    b.booking_id AS bookingId,\n" +
                    "    b.booking_code AS bookingCode,\n" +
                    "    s.specialization_name AS specializationName,\n" +
                    "    b.first_name AS firstName,\n" +
                    "    b.last_name AS lastName,\n" +
                    "    DATE_FORMAT(b.date_of_birth, '%d-%m-%Y') AS dateOfBirth,\n" +
                    "    CASE \n" +
                    "        WHEN b.gender = 1 THEN 'Male'\n" +
                    "        WHEN b.gender = 0 THEN 'Female'\n" +
                    "        ELSE 'Other'\n" +
                    "    END AS gender,\n" +
                    "    b.phone_number AS phoneNumber,\n" +
                    "    CONCAT(a.specific_address, ', ', w.ward_name, ', ', d.district_name, ', ', c.city_name) AS userAddress,\n" +
                    "    b.describe_symptoms AS describeSymptoms,\n" +
                    "    DATE_FORMAT(dow.working_day, '%d-%m-%Y') AS workingDay,\n" +
                    "    TIME_FORMAT(ws.start_time, '%H:%i') AS startTime,\n" +
                    "    TIME_FORMAT(ws.end_time, '%H:%i') AS endTime,\n" +
                    "    b.status AS status,\n" +
                    "    DATE_FORMAT(b.created_at, '%d-%m-%Y %H:%i:%s') AS createdAt\n" +
                    "FROM booking AS b\n" +
                    "INNER JOIN address AS a ON b.address_id = a.address_id\n" +
                    "INNER JOIN ward AS w ON a.ward_id = w.ward_id\n" +
                    "INNER JOIN district AS d ON w.district_id = d.district_id\n" +
                    "INNER JOIN city AS c ON d.city_id = c.city_id\n" +
                    "INNER JOIN work_schedule AS ws ON b.work_schedule_id = ws.work_schedule_id\n" +
                    "INNER JOIN day_of_week AS dow ON ws.day_of_week_id = dow.day_of_week_id\n" +
                    "INNER JOIN doctor AS dt ON dow.doctor_id = dt.doctor_id\n" +
                    "INNER JOIN user AS u ON dt.user_id = u.user_id\n" +
                    "INNER JOIN specialization AS s ON dt.specialization_id = s.specialization_id\n" +
                    "WHERE dt.doctor_id = :doctorId\n" +
                    "ORDER BY dow.working_day ASC",
            countQuery = "SELECT COUNT(*) AS totalBookings\n" +
                    "FROM booking AS b\n" +
                    "INNER JOIN work_schedule AS ws \n" +
                    "    ON b.work_schedule_id = ws.work_schedule_id\n" +
                    "INNER JOIN day_of_week AS dow \n" +
                    "    ON ws.day_of_week_id = dow.day_of_week_id\n" +
                    "INNER JOIN doctor AS dt \n" +
                    "    ON dow.doctor_id = dt.doctor_id\n" +
                    "WHERE dt.doctor_id = :doctorId\n",
            nativeQuery = true
    )
    Page<BookingDetailsInfoProjection> getBookingDetailsByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query(value = "SELECT\n" +
            "    b.booking_id AS bookingId,\n" +
            "    b.booking_code AS bookingCode,\n" +
            "    s.specialization_name AS specializationName,\n" +
            "    b.first_name AS firstName,\n" +
            "    b.last_name AS lastName,\n" +
            "    DATE_FORMAT(b.date_of_birth, '%d-%m-%Y') AS dateOfBirth,\n" +
            "    CASE\n" +
            "        WHEN b.gender = 1 THEN 'Male'\n" +
            "        WHEN b.gender = 0 THEN 'Female'\n" +
            "        ELSE 'Other'\n" +
            "    END AS gender,\n" +
            "    b.phone_number AS phoneNumber,\n" +
            "    CONCAT(a.specific_address, ', ', w.ward_name, ', ', d.district_name, ', ', c.city_name) AS userAddress,\n" +
            "    b.describe_symptoms AS describeSymptoms,\n" +
            "    DATE_FORMAT(dow.working_day, '%d-%m-%Y') AS workingDay,\n" +
            "    TIME_FORMAT(ws.start_time, '%H:%i') AS startTime,\n" +
            "    TIME_FORMAT(ws.end_time, '%H:%i') AS endTime,\n" +
            "    b.status AS status,\n" +
            "    DATE_FORMAT(b.created_at, '%d-%m-%Y %H:%i:%s') AS createdAt\n" +
            "FROM booking AS b\n" +
            "INNER JOIN address AS a ON b.address_id = a.address_id\n" +
            "INNER JOIN ward AS w ON a.ward_id = w.ward_id\n" +
            "INNER JOIN district AS d ON w.district_id = d.district_id\n" +
            "INNER JOIN city AS c ON d.city_id = c.city_id\n" +
            "INNER JOIN work_schedule AS ws ON b.work_schedule_id = ws.work_schedule_id\n" +
            "INNER JOIN day_of_week AS dow ON ws.day_of_week_id = dow.day_of_week_id\n" +
            "INNER JOIN doctor AS dt ON dow.doctor_id = dt.doctor_id\n" +
            "INNER JOIN user AS u ON dt.user_id = u.user_id\n" +
            "INNER JOIN specialization AS s ON dt.specialization_id = s.specialization_id\n" +
            "WHERE b.booking_id = :bookingId",
            nativeQuery = true)
    BookingDetailsInfoProjection getBookingDetail(@Param("bookingId") Long bookingId);

    @Query(
            value = "SELECT \n" +
                    "    b.booking_id AS bookingId,\n" +
                    "    b.booking_code AS bookingCode,\n" +
                    "    s.specialization_name AS specializationName,\n" +
                    "    b.first_name AS firstName,\n" +
                    "    b.last_name AS lastName,\n" +
                    "    DATE_FORMAT(b.date_of_birth, '%d-%m-%Y') AS dateOfBirth,\n" +
                    "    CASE \n" +
                    "        WHEN b.gender = 1 THEN 'Male'\n" +
                    "        WHEN b.gender = 0 THEN 'Female'\n" +
                    "        ELSE 'Other'\n" +
                    "    END AS gender,\n" +
                    "    b.phone_number AS phoneNumber,\n" +
                    "    CONCAT(a.specific_address, ', ', w.ward_name, ', ', d.district_name, ', ', c.city_name) AS userAddress,\n" +
                    "    b.describe_symptoms AS describeSymptoms,\n" +
                    "    DATE_FORMAT(dow.working_day, '%d-%m-%Y') AS workingDay,\n" +
                    "    TIME_FORMAT(ws.start_time, '%H:%i') AS startTime,\n" +
                    "    TIME_FORMAT(ws.end_time, '%H:%i') AS endTime,\n" +
                    "    b.status AS status,\n" +
                    "    DATE_FORMAT(b.created_at, '%d-%m-%Y %H:%i:%s') AS createdAt\n" +
                    "FROM booking AS b\n" +
                    "INNER JOIN address AS a ON b.address_id = a.address_id\n" +
                    "INNER JOIN ward AS w ON a.ward_id = w.ward_id\n" +
                    "INNER JOIN district AS d ON w.district_id = d.district_id\n" +
                    "INNER JOIN city AS c ON d.city_id = c.city_id\n" +
                    "INNER JOIN work_schedule AS ws ON b.work_schedule_id = ws.work_schedule_id\n" +
                    "INNER JOIN day_of_week AS dow ON ws.day_of_week_id = dow.day_of_week_id\n" +
                    "INNER JOIN doctor AS dt ON dow.doctor_id = dt.doctor_id\n" +
                    "INNER JOIN user AS u ON dt.user_id = u.user_id\n" +
                    "INNER JOIN specialization AS s ON dt.specialization_id = s.specialization_id\n" +
                    "ORDER BY b.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM booking",
            nativeQuery = true
    )
    Page<BookingDetailsInfoProjection> getAllBookings(Pageable pageable);

    @Query(value = "SELECT dt.doctor_id\n" +
            "FROM booking AS b\n" +
            "INNER JOIN work_schedule AS ws \n" +
            "    ON b.work_schedule_id = ws.work_schedule_id\n" +
            "INNER JOIN day_of_week AS dow \n" +
            "    ON ws.day_of_week_id = dow.day_of_week_id\n" +
            "INNER JOIN doctor AS dt \n" +
            "    ON dow.doctor_id = dt.doctor_id\n" +
            "WHERE b.booking_id = :bookingId", nativeQuery = true)
    long getDoctorIdByBookingId(@Param("bookingId") Long bookingId);

    @Query(value = "SELECT \n" +
            "    b.booking_id AS bookingId,\n" +
            "    DATE(dow.working_day) AS workingDay,\n" +
            "    ws.start_time AS startTime,\n" +
            "    ws.end_time   AS endTime\n" +
            "FROM booking b\n" +
            "JOIN work_schedule ws ON b.work_schedule_id = ws.work_schedule_id\n" +
            "JOIN day_of_week  dow ON ws.day_of_week_id  = dow.day_of_week_id\n" +
            "WHERE \n" +
            "    dow.doctor_id = :doctorId\n" +
            "    AND b.status IN ('CONFIRMED')\n" +
            "    AND CONCAT(dow.working_day, ' ', ws.start_time) <= :timeOffStart\n" +
            "    AND CONCAT(dow.working_day, ' ', ws.end_time)   >= :timeOffEnd", nativeQuery = true)
    List<BookingTimeInfoProjection> findBookingsOverlappingDoctorTimeOff(@Param("doctorId") Long doctorId,
                                                                         @Param("timeOffStart")LocalDateTime timeOffStart,
                                                                         @Param("timeOffEnd")LocalDateTime timeOffEnd);
}
