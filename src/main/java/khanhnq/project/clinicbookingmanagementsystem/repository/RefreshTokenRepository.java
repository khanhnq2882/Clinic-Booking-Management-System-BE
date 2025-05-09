package khanhnq.project.clinicbookingmanagementsystem.repository;

import khanhnq.project.clinicbookingmanagementsystem.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query(value = "select * from refresh_token where user_id = :userId", nativeQuery = true)
    RefreshToken findRefreshTokenByUserId(@Param("userId") Long userId);

    RefreshToken findRefreshTokenByToken(String token);
}
