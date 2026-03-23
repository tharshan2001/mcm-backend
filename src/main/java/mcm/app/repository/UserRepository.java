package mcm.app.repository;

import mcm.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.usedCoupons WHERE u.id = :id")
    Optional<User> findByIdWithUsedCoupons(Long id);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.usedCoupons c WHERE u.id = :userId AND c.id = :couponId")
    boolean hasUserUsedCoupon(Long userId, Long couponId);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'CUSTOMER'")
    Long countCustomers();
}