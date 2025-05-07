package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.Doctor;
import khanhnq.project.clinicbookingmanagementsystem.model.dto.DoctorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query(value = "select * from doctor where user_id = :userId", nativeQuery = true)
    Doctor findDoctorByUserId(Long userId);

    @Query(value = "select * from doctor where specialization_id = :specializationId", nativeQuery = true)
    List<Doctor> getDoctorsBySpecializationId(@Param("specializationId") Long specializationId);

    @Query(value = "select d.doctor_id as doctorId, u.user_code as userCode, u.first_name as firstName, u.last_name as lastName, \n" +
            "s.specialization_name as specializationName, d.education_level as educationLevel, d.biography as biography, \n" +
            "d.career_description as careerDescription, we.position as position, we.work_specialization_name as workSpecializationName, \n" +
            "we.work_place as workPlace, we.year_of_start_work as yearOfStartWork, we.year_of_end_work as yearOfEndWork, \n" +
            "we.description as description, f.file_id as fileId, f.file_type as fileType, f.file_name as fileName, dow.working_day as workingDay, \n" +
            "ws.start_time as startTime, ws.end_time as endTime\n" +
            "from doctor as d\n" +
            "left join user as u on d.user_id = u.user_id\n" +
            "left join specialization as s on d.specialization_id = s.specialization_id\n" +
            "left join work_experience as we on d.doctor_id = we.doctor_id\n" +
            "left join file as f on u.user_id = f.user_id\n" +
            "left join day_of_week as dow on d.doctor_id = dow.doctor_id\n" +
            "left join work_schedule as ws on dow.day_of_week_id = ws.day_of_week_id\n" +
            "where u.status = 'ACTIVE' and d.specialization_id = :specializationId and ws.work_schedule_id not in (" +
            "select ws.work_schedule_id from work_schedule as ws " +
            "inner join booking as b " +
            "on ws.work_schedule_id = b.work_schedule_id where b.status not in ('CANCELLED'))"
            , nativeQuery = true)
    List<DoctorInfo> getDoctorsBySpecialization(@Param("specializationId") Long specializationId);

    @Query(value = "select d.doctor_id as doctorId, u.user_code as userCode, u.first_name as firstName, u.last_name as lastName, \n" +
            "s.specialization_name as specializationName, d.education_level as educationLevel, d.biography as biography, \n" +
            "d.career_description as careerDescription, we.position as position, we.work_specialization_name as workSpecializationName, \n" +
            "we.work_place as workPlace, we.year_of_start_work as yearOfStartWork, we.year_of_end_work as yearOfEndWork, \n" +
            "we.description as description, f.file_id as fileId, f.file_type as fileType, f.file_name as fileName, dow.working_day as workingDay, \n" +
            "ws.start_time as startTime, ws.end_time as endTime\n" +
            "from doctor as d\n" +
            "left join user as u on d.user_id = u.user_id\n" +
            "left join specialization as s on d.specialization_id = s.specialization_id\n" +
            "left join work_experience as we on d.doctor_id = we.doctor_id\n" +
            "left join file as f on u.user_id = f.user_id\n" +
            "left join day_of_week as dow on d.doctor_id = dow.doctor_id\n" +
            "left join work_schedule as ws on dow.day_of_week_id = ws.day_of_week_id\n" +
            "where u.status = 'ACTIVE' and d.doctor_id = :doctorId and ws.work_schedule_id not in (" +
            "select ws.work_schedule_id from work_schedule as ws " +
            "inner join booking as b " +
            "on ws.work_schedule_id = b.work_schedule_id where b.status not in ('CANCELLED'))"
            , nativeQuery = true)
    List<DoctorInfo> getDoctorDetails(@Param("doctorId") Long doctorId);

}
