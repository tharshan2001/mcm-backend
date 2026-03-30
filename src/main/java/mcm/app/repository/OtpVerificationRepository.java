package mcm.app.repository;

import mcm.app.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailAndVerifiedFalseOrderByExpiresAtDesc(String email);

    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.email = :email")
    void deleteByEmail(String email);
}
