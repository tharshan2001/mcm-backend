package mcm.app.repository;

import mcm.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query(value = "SELECT p.id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.archived, p.category_id " +
            "FROM product p " +
            "JOIN product_category c ON p.category_id = c.id " +
            "WHERE c.name = :categoryName AND p.archived = false " +
            "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Product> findRelatedByCategoryName(@Param("categoryName") String categoryName, @Param("limit") int limit);

    @Query(value = "SELECT p.id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.archived, p.category_id " +
            "FROM product p " +
            "JOIN product_category c ON p.category_id = c.id " +
            "WHERE c.name = :categoryName AND p.name LIKE %:name% AND p.archived = false " +
            "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Product> findRelatedByCategoryNameAndName(@Param("categoryName") String categoryName, @Param("name") String name, @Param("limit") int limit);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.archived = false")
    Long countActiveProducts();

    @Query(value = "SELECT p.id, p.name, p.slug, p.description, p.price, p.stock_quantity, p.archived, p.category_id " +
            "FROM product p " +
            "LEFT JOIN order_item oi ON oi.product_id = p.id " +
            "LEFT JOIN orders o ON o.id = oi.order_id AND o.order_date >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "WHERE p.archived = false " +
            "GROUP BY p.id " +
            "ORDER BY COALESCE(SUM(oi.quantity), 0) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Product> findTrendingProducts(@Param("limit") int limit);

    @Query(value = "SELECT * FROM product WHERE archived = false ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Product> findRandomProducts(@Param("limit") int limit);

    @Query("SELECT p FROM Product p WHERE p.archived = false " +
            "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:minStock IS NULL OR p.stockQuantity >= :minStock) " +
            "AND (:maxStock IS NULL OR p.stockQuantity <= :maxStock)")
    List<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock
    );
}