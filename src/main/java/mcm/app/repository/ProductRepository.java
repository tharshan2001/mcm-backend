package mcm.app.repository;

import mcm.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);

    // Infinite scroll query
    List<Product> findTop10ByIdGreaterThanOrderByIdAsc(Long id);

    // First load
    List<Product> findTop10ByOrderByIdAsc();

    List<Product> findByArchivedFalse();

}