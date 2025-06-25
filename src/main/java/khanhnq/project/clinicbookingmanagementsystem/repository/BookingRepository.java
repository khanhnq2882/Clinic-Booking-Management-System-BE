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
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = "select b.booking_id as bookingId, dof.working_day as workingDay, ws.start_time as startTime, ws.end_time as endTime \n" +
            "from booking as b\n" +
            "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
            "inner join day_of_week as dof on ws.day_of_week_id = dof.day_of_week_id\n" +
            "where b.work_schedule_id = :workScheduleId and b.status not in ('CANCELLED')", nativeQuery = true)
    List<BookingTimeInfoProjection> getBookingsByWorkScheduleId(@Param("workScheduleId") Long workScheduleId);

    @Query(value = "select b.booking_id as bookingId, b.created_at as createdAt, dof.working_day as workingDay, " +
            "ws.start_time as startTime, ws.end_time as endTime \n" +
            "from booking as b\n" +
            "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
            "inner join day_of_week as dof on ws.day_of_week_id = dof.day_of_week_id\n" +
            "where b.booking_id = :bookingId", nativeQuery = true)
    BookingTimeInfoProjection getBookingInfoByBookingId(@Param("bookingId") Long bookingId);

    @Modifying
    @Transactional
    @Query(value = "delete from booking where user_id is null " +
            "and status not in ('CONFIRMED') and created_at < current_timestamp() - interval ?1 hour", nativeQuery = true)
    void deleteUnconfirmedBookingAfter24Hours(int hours);

    @Modifying
    @Transactional
    @Query(value = "delete from booking where user_id is null and created_at < current_timestamp() - interval ?1 day", nativeQuery = true)
    void deleteOlderThanDays(int days);

    @Query(
            value = "select \n" +
                    "    b.booking_id as bookingId, \n" +
                    "    b.booking_code as bookingCode, \n" +
                    "    s.specialization_name as specializationName, \n" +
                    "    b.first_name as firstName, \n" +
                    "    b.last_name as lastName, \n" +
                    "    DATE_FORMAT(b.date_of_birth, '%d-%m-%Y') as dateOfBirth, \n" +
                    "    CASE \n" +
                    "        WHEN b.gender = 1 THEN 'Male'\n" +
                    "        WHEN b.gender = 0 THEN 'Female'\n" +
                    "        ELSE 'Other'\n" +
                    "    END AS gender,\n" +
                    "    b.phone_number as phoneNumber, \n" +
                    "    concat(a.specific_address, ', ', w.ward_name, ', ', d.district_name, ', ', c.city_name) as userAddress,\n" +
                    "    b.describe_symptoms as describeSymptoms, \n" +
                    "    DATE_FORMAT(dow.working_day, '%d-%m-%Y') as workingDay, \n" +
                    "    TIME_FORMAT(ws.start_time, '%H:%i') as startTime, \n" +
                    "    TIME_FORMAT(ws.end_time, '%H:%i') as endTime, \n" +
                    "    b.status as status, \n" +
                    "    DATE_FORMAT(b.created_at, '%d-%m-%Y %H:%i:%s') as createdAt\n" +
                    "from booking as b\n" +
                    "inner join address as a on b.address_id = a.address_id\n" +
                    "inner join ward as w on a.ward_id = w.ward_id\n" +
                    "inner join district as d on w.district_id = d.district_id\n" +
                    "inner join city as c on d.city_id = c.city_id\n" +
                    "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
                    "inner join day_of_week as dow on ws.day_of_week_id = dow.day_of_week_id\n" +
                    "inner join doctor as dt on dow.doctor_id = dt.doctor_id\n" +
                    "inner join user as u on dt.user_id = u.user_id\n" +
                    "inner join specialization as s on dt.specialization_id = s.specialization_id\n" +
                    "where b.user_id = :userId order by b.created_at desc",
            countQuery = "SELECT COUNT(*) FROM booking WHERE user_id = :userId",
            nativeQuery = true
    )
    Page<BookingDetailsInfoProjection> getBookingDetailsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            value = "select \n" +
                    "    b.booking_id as bookingId, \n" +
                    "    b.booking_code as bookingCode, \n" +
                    "    s.specialization_name as specializationName, \n" +
                    "    b.first_name as firstName, \n" +
                    "    b.last_name as lastName, \n" +
                    "    DATE_FORMAT(b.date_of_birth, '%d-%m-%Y') as dateOfBirth, \n" +
                    "    CASE \n" +
                    "        WHEN b.gender = 1 THEN 'Male'\n" +
                    "        WHEN b.gender = 0 THEN 'Female'\n" +
                    "        ELSE 'Other'\n" +
                    "    END AS gender,\n" +
                    "    b.phone_number as phoneNumber, \n" +
                    "    concat(a.specific_address, ', ', w.ward_name, ', ', d.district_name, ', ', c.city_name) as userAddress,\n" +
                    "    b.describe_symptoms as describeSymptoms, \n" +
                    "    DATE_FORMAT(dow.working_day, '%d-%m-%Y') as workingDay, \n" +
                    "    TIME_FORMAT(ws.start_time, '%H:%i') as startTime, \n" +
                    "    TIME_FORMAT(ws.end_time, '%H:%i') as endTime, \n" +
                    "    b.status as status, \n" +
                    "    DATE_FORMAT(b.created_at, '%d-%m-%Y %H:%i:%s') as createdAt\n" +
                    "from booking as b\n" +
                    "inner join address as a on b.address_id = a.address_id\n" +
                    "inner join ward as w on a.ward_id = w.ward_id\n" +
                    "inner join district as d on w.district_id = d.district_id\n" +
                    "inner join city as c on d.city_id = c.city_id\n" +
                    "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
                    "inner join day_of_week as dow on ws.day_of_week_id = dow.day_of_week_id\n" +
                    "inner join doctor as dt on dow.doctor_id = dt.doctor_id\n" +
                    "inner join user as u on dt.user_id = u.user_id\n" +
                    "inner join specialization as s on dt.specialization_id = s.specialization_id\n" +
                    "where dt.doctor_id = :doctorId order by dow.working_day asc",
            countQuery = "select count(*)\n" +
                    "from booking as b\n" +
                    "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
                    "inner join day_of_week as dow on ws.day_of_week_id = dow.day_of_week_id\n" +
                    "inner join doctor as dt on dow.doctor_id = dt.doctor_id\n" +
                    "where dt.doctor_id = :doctorId",
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
            "WHERE b.booking_id = :bookingId", nativeQuery = true)
    BookingDetailsInfoProjection getBookingDetail(@Param("bookingId") Long bookingId);

    @Query(
            value = "select \n" +
                    "    b.booking_id as bookingId, \n" +
                    "    b.booking_code as bookingCode, \n" +
                    "    s.specialization_name as specializationName, \n" +
                    "    b.first_name as firstName, \n" +
                    "    b.last_name as lastName, \n" +
                    "    DATE_FORMAT(b.date_of_birth, '%d-%m-%Y') as dateOfBirth, \n" +
                    "    CASE \n" +
                    "        WHEN b.gender = 1 THEN 'Male'\n" +
                    "        WHEN b.gender = 0 THEN 'Female'\n" +
                    "        ELSE 'Other'\n" +
                    "    END AS gender,\n" +
                    "    b.phone_number as phoneNumber, \n" +
                    "    concat(a.specific_address, ', ', w.ward_name, ', ', d.district_name, ', ', c.city_name) as userAddress,\n" +
                    "    b.describe_symptoms as describeSymptoms, \n" +
                    "    DATE_FORMAT(dow.working_day, '%d-%m-%Y') as workingDay, \n" +
                    "    TIME_FORMAT(ws.start_time, '%H:%i') as startTime, \n" +
                    "    TIME_FORMAT(ws.end_time, '%H:%i') as endTime, \n" +
                    "    b.status as status, \n" +
                    "    DATE_FORMAT(b.created_at, '%d-%m-%Y %H:%i:%s') as createdAt\n" +
                    "from booking as b\n" +
                    "inner join address as a on b.address_id = a.address_id\n" +
                    "inner join ward as w on a.ward_id = w.ward_id\n" +
                    "inner join district as d on w.district_id = d.district_id\n" +
                    "inner join city as c on d.city_id = c.city_id\n" +
                    "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
                    "inner join day_of_week as dow on ws.day_of_week_id = dow.day_of_week_id\n" +
                    "inner join doctor as dt on dow.doctor_id = dt.doctor_id\n" +
                    "inner join user as u on dt.user_id = u.user_id\n" +
                    "inner join specialization as s on dt.specialization_id = s.specialization_id\n" +
                    "order by b.created_at desc",
            countQuery = "SELECT COUNT(*) FROM booking",
            nativeQuery = true
    )
    Page<BookingDetailsInfoProjection> getAllBookings(Pageable pageable);

    @Query(value = "select dt.doctor_id\n" +
            "from booking as b\n" +
            "inner join work_schedule as ws on b.work_schedule_id = ws.work_schedule_id\n" +
            "inner join day_of_week as dow on ws.day_of_week_id = dow.day_of_week_id\n" +
            "inner join doctor as dt on dow.doctor_id = dt.doctor_id\n" +
            "where b.booking_id = :bookingId", nativeQuery = true)
    long getDoctorIdByBookingId(@Param("bookingId") Long bookingId);

}
