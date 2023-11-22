package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    @Query(value = "SELECT u FROM User AS u INNER JOIN u.roles AS r WHERE r.roleName = 'ROLE_USER' OR r.roleName = 'ROLE_DOCTOR'")
    List<User> getAllUsers();
    @Query(value = "SELECT u FROM User AS u INNER JOIN u.roles AS r WHERE r.roleName = 'ROLE_DOCTOR'")
    List<User> getAllDoctors();
}
