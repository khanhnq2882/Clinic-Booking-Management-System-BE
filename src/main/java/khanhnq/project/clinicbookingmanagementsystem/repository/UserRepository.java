package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);

    User findUserByEmail(String email);

    @Query(value = "SELECT u FROM User AS u INNER JOIN u.roles AS r WHERE r.roleName = 'ROLE_USER'")
    Page<User> getAllUsers(Pageable pageable);

    @Query(value = "SELECT u FROM User AS u INNER JOIN u.roles AS r WHERE r.roleName = 'ROLE_USER'")
    List<User> getUsers();

    @Query(value = "SELECT u FROM User AS u INNER JOIN u.roles AS r WHERE r.roleName = 'ROLE_DOCTOR'")
    List<User> getDoctors();

    @Query(value = "SELECT u FROM User AS u INNER JOIN u.roles AS r WHERE r.roleName = 'ROLE_ADMIN'")
    List<User> getAdmins();

    @Query(value = "SELECT u FROM User AS u")
    User getUserFromExcel(Long specializationId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);
}
