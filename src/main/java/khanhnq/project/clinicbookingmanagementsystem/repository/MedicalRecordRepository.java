package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    @Query(value = "SELECT m FROM MedicalRecord m WHERE m.booking.bookingId = :bookingId")
    MedicalRecord findMedicalRecordByBooking(@Param("bookingId") Long bookingId);
}
