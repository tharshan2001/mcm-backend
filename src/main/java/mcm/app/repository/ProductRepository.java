package mcm.app.repository;

import mcm.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);

    // Infinite scroll query
    List<Product> findTop10ByIdGreaterThanOrderByIdAsc(Long id);

    // First load
    List<Product> findTop10ByOrderByIdAsc();

    List<Product> findByArchivedFalse();

    // New method: random related products from same category, excluding current product
    @Query(value = "SELECT * FROM product WHERE category_id = :categoryId AND id <> :productId AND archived = false ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Product> findRandomRelatedProducts(@Param("categoryId") Long categoryId,
                                            @Param("productId") Long productId,
                                            @Param("limit") int limit);
}