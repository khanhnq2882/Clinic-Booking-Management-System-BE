package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.Doctor;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorDetailsInfoProjection;
import khanhnq.project.clinicbookingmanagementsystem.model.projection.DoctorInfoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query(value = "select * from doctor where user_id = :userId", nativeQuery = true)
    Doctor findDoctorByUserId(Long userId);

    @Query(value = "select * from doctor where specialization_id = :specializationId", nativeQuery = true)
    List<Doctor> getDoctorsBySpecializationId(@Param("specializationId") Long specializationId);

    @Query(value = "SELECT \n" +
            "    d.doctor_id AS doctorId,\n" +
            "    u.user_code AS userCode,\n" +
            "    u.first_name AS firstName,\n" +
            "    u.last_name AS lastName,\n" +
            "    s.specialization_name AS specializationName,\n" +
            "    d.education_level AS educationLevel,\n" +
            "    d.biography AS biography,\n" +
            "    d.career_description AS careerDescription,\n" +
            "    we.position AS position,\n" +
            "    we.work_specialization_name AS workSpecializationName,\n" +
            "    we.work_place AS workPlace,\n" +
            "    we.year_of_start_work AS yearOfStartWork,\n" +
            "    we.year_of_end_work AS yearOfEndWork,\n" +
            "    we.description AS description,\n" +
            "    f.file_id AS fileId,\n" +
            "    f.file_type AS fileType,\n" +
            "    f.file_name AS fileName,\n" +
            "    f.file_path AS filePath,\n" +
            "    dow.working_day AS workingDay,\n" +
            "    ws.start_time AS startTime,\n" +
            "    ws.end_time AS endTime\n" +
            "FROM \n" +
            "    doctor d\n" +
            "LEFT JOIN user u ON d.user_id = u.user_id\n" +
            "LEFT JOIN specialization s ON d.specialization_id = s.specialization_id\n" +
            "LEFT JOIN work_experience we ON d.doctor_id = we.doctor_id\n" +
            "LEFT JOIN file f ON u.user_id = f.user_id\n" +
            "LEFT JOIN day_of_week dow ON d.doctor_id = dow.doctor_id\n" +
            "LEFT JOIN work_schedule ws ON dow.day_of_week_id = ws.day_of_week_id\n" +
            "WHERE \n" +
            "    u.status = 'ACTIVE'\n" +
            "    AND d.specialization_id = :specializationId\n" +
            "    AND (\n" +
            "        ws.work_schedule_id IS NULL\n" +
            "        OR ws.work_schedule_id NOT IN (\n" +
            "            SELECT b.work_schedule_id\n" +
            "            FROM booking b\n" +
            "            JOIN work_schedule ws ON b.work_schedule_id = ws.work_schedule_id\n" +
            "            WHERE b.status NOT IN ('CANCELLED')\n" +
            "        )\n" +
            "    );\n"
            , nativeQuery = true)
    List<DoctorDetailsInfoProjection> getDoctorsBySpecialization(@Param("specializationId") Long specializationId);

    @Query(value = "SELECT \n" +
            "    d.doctor_id AS doctorId,\n" +
            "    u.user_code AS userCode,\n" +
            "    u.first_name AS firstName,\n" +
            "    u.last_name AS lastName,\n" +
            "    s.specialization_name AS specializationName,\n" +
            "    d.education_level AS educationLevel,\n" +
            "    d.biography AS biography,\n" +
            "    d.career_description AS careerDescription,\n" +
            "    we.position AS position,\n" +
            "    we.work_specialization_name AS workSpecializationName,\n" +
            "    we.work_place AS workPlace,\n" +
            "    we.year_of_start_work AS yearOfStartWork,\n" +
            "    we.year_of_end_work AS yearOfEndWork,\n" +
            "    we.description AS description,\n" +
            "    f.file_id AS fileId,\n" +
            "    f.file_type AS fileType,\n" +
            "    f.file_name AS fileName,\n" +
            "    f.file_path AS filePath,\n" +
            "    dow.working_day AS workingDay,\n" +
            "    ws.start_time AS startTime,\n" +
            "    ws.end_time AS endTime\n" +
            "FROM \n" +
            "    doctor d\n" +
            "LEFT JOIN user u ON d.user_id = u.user_id\n" +
            "LEFT JOIN specialization s ON d.specialization_id = s.specialization_id\n" +
            "LEFT JOIN work_experience we ON d.doctor_id = we.doctor_id\n" +
            "LEFT JOIN file f ON u.user_id = f.user_id\n" +
            "LEFT JOIN day_of_week dow ON d.doctor_id = dow.doctor_id\n" +
            "LEFT JOIN work_schedule ws ON dow.day_of_week_id = ws.day_of_week_id\n" +
            "WHERE \n" +
            "    u.status = 'ACTIVE'\n" +
            "    AND d.doctor_id = :doctorId\n" +
            "    AND (\n" +
            "        ws.work_schedule_id IS NULL \n" +
            "        OR ws.work_schedule_id NOT IN (\n" +
            "            SELECT b.work_schedule_id\n" +
            "            FROM booking b\n" +
            "            JOIN work_schedule ws ON b.work_schedule_id = ws.work_schedule_id\n" +
            "            WHERE b.status NOT IN ('CANCELLED')\n" +
            "        )\n" +
            "    )"
            , nativeQuery = true)
    List<DoctorDetailsInfoProjection> getDoctorDetails(@Param("doctorId") Long doctorId);

    @Query(value = "select \n" +
            "    d.doctor_id as doctorId, \n" +
            "    u.user_code as userCode, \n" +
            "    u.first_name as firstName, \n" +
            "    u.last_name as lastName, \n" +
            "    u.email as email, \n" +
            "    u.phone_number as phoneNumber, \n" +
            "    d.education_level as educationLevel, \n" +
            "    s.specialization_name as specializationName, \n" +
            "    u.status as status, \n" +
            "    f.file_id as fileId, \n" +
            "    f.file_type as fileType, \n" +
            "    f.file_name as fileName, \n" +
            "    f.file_path as filePath, \n" +
            "    DATE_FORMAT(d.created_at, '%d-%m-%Y %H:%i:%s') as createdAt\n" +
            "from doctor as d\n" +
            "left join user as u on d.user_id = u.user_id\n" +
            "left join specialization as s on d.specialization_id = s.specialization_id\n" +
            "left join file as f on u.user_id = f.user_id"
            , countQuery = "SELECT COUNT(*) FROM doctor"
            , nativeQuery = true)
    Page<DoctorInfoProjection> getDoctorsInfo(Pageable pageable);

}
