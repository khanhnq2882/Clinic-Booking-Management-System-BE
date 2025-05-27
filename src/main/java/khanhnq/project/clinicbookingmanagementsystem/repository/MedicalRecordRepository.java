package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.MedicalRecord;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.MedicalRecordDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    @Query(value = "SELECT m FROM MedicalRecord m WHERE m.booking.bookingId = :bookingId")
    MedicalRecord findMedicalRecordByBooking(@Param("bookingId") Long bookingId);

    @Query(value = "SELECT \n" +
            "    mr.medical_record_id AS medicalRecordId,\n" +
            "    b.booking_id AS bookingId,\n" +
            "    b.first_name AS firstName,\n" +
            "    b.last_name AS lastName,\n" +
            "    b.date_of_birth AS dateOfBirth,\n" +
            "    CASE WHEN b.gender = 1 THEN 'MALE'\n" +
            "         ELSE 'FEMALE' \n" +
            "    END AS gender,\n" +
            "    b.phone_number AS phoneNumber,\n" +
            "    a.specific_address AS specificAddress,\n" +
            "    w.ward_name AS wardName,\n" +
            "    d.district_name AS districtName,\n" +
            "    c.city_name AS cityName,\n" +
            "    mr.visit_date AS visitDate,\n" +
            "    mr.reason_for_visit AS reasonForVisit,\n" +
            "    mr.medical_history AS medicalHistory,\n" +
            "    mr.allergies AS allergies,\n" +
            "    mr.diagnosis AS diagnosis,\n" +
            "    mr.treatment_plan AS treatmentPlan,\n" +
            "    mr.prescribed_medications AS prescribedMedications,\n" +
            "    mr.follow_up_instructions AS followUpInstructions,\n" +
            "    mr.next_appointment_date AS nextAppointmentDate,\n" +
            "    mr.consultation_notes AS consultationNotes,\n" +
            "    mr.status AS medicalRecordStatus,\n" +
            "\tlr.lab_result_id as labResultId,\n" +
            "\ttp.test_package_name AS testPackageName,\n" +
            "    lr.sample_collection_time AS sampleCollectionTime,\n" +
            "    lr.sample_reception_time AS sampleReceptionTime,\n" +
            "    lr.test_date AS testDate,\n" +
            "    lr.result_delivery_date AS resultDeliveryDate,\n" +
            "    lr.note AS labNote,\n" +
            "    dt.education_level as educationLevel,\n" +
            "    u.first_name as doctorFirstName,\n" +
            "    u.last_name as doctorLastName,\n" +
            "    lr.status AS labStatus,\n" +
            "    tpa.name AS testAttributeName,\n" +
            "    tpa.unit AS unit,\n" +
            "    tpa.attribute_metadata AS attributeMetadata,\n" +
            "    tr.result AS testResult,\n" +
            "    tr.note AS testResultNote,\n" +
            "    tr.status AS testResultStatus,\n" +
            "    nr.min_value AS normalMinValue,\n" +
            "    nr.max_value AS normalMaxValue,\n" +
            "    nr.equal_value AS normalEqualValue,\n" +
            "    nr.expected_value AS normalExpectedValue,\n" +
            "    nr.normal_range_type as normalRangeType\n" +
            "FROM medical_record mr\n" +
            "INNER JOIN booking b ON mr.booking_id = b.booking_id\n" +
            "INNER JOIN address a ON b.address_id = a.address_id\n" +
            "INNER JOIN ward w ON a.ward_id = w.ward_id\n" +
            "INNER JOIN district d ON w.district_id = d.district_id\n" +
            "INNER JOIN city c ON d.city_id = c.city_id\n" +
            "INNER JOIN lab_result lr ON mr.medical_record_id = lr.medical_record_id\n" +
            "INNER JOIN doctor dt ON lr.doctor_id = dt.doctor_id\n" +
            "INNER JOIN user u ON dt.user_id = u.user_id\n" +
            "INNER JOIN test_package tp ON lr.test_package_id = tp.test_package_id\n" +
            "INNER JOIN test_result tr ON tr.lab_result_id = lr.lab_result_id\n" +
            "INNER JOIN test_package_attribute tpa ON tr.test_package_attribute_id = tpa.test_package_attribute_id\n" +
            "LEFT JOIN LATERAL (\n" +
            "    SELECT *\n" +
            "    FROM normal_range nr_sub\n" +
            "    WHERE nr_sub.test_package_attribute_id = tpa.test_package_attribute_id\n" +
            "      AND (\n" +
            "        (\n" +
            "\t\t  nr_sub.normal_range_type IN ('RANGE', 'LESS_THAN','LESS_THAN_EQUAL','GREATER_THAN', 'GREATER_THAN_EQUAL', 'EQUAL')\n" +
            "\t\t  AND (nr_sub.gender IS NULL OR nr_sub.gender = CASE WHEN b.gender = 1 THEN 'MALE' ELSE 'FEMALE' END)\n" +
            "\t\t  AND TIMESTAMPDIFF(YEAR, b.date_of_birth, CURDATE()) BETWEEN nr_sub.age_min AND nr_sub.age_max\n" +
            "\t\t)\n" +
            "        OR nr_sub.normal_range_type IN ('QUALITATIVE','SEMI_QUALITATIVE','TEXT')\n" +
            "      )\n" +
            "    ORDER BY nr_sub.age_min DESC\n" +
            "    LIMIT 1\n" +
            ") nr ON true\n", nativeQuery = true)
    List<MedicalRecordDetailsProjection> getAllMedicalRecordsDetails();
}
