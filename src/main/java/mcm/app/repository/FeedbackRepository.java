package mcm.app.repository;

import mcm.app.entity.Feedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByProductId(Long productId);

    // First page (latest feedbacks)
    @Query("SELECT f FROM Feedback f WHERE f.product.id = :productId ORDER BY f.id DESC")
    List<Feedback> findByProductIdOrderByIdDesc(@Param("productId") Long productId, Pageable pageable);

    // Next pages (feedbacks with id < cursor)
    @Query("SELECT f FROM Feedback f WHERE f.product.id = :productId AND f.id < :cursor ORDER BY f.id DESC")
    List<Feedback> findByProductIdAndIdLessThanOrderByIdDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}