package mcm.app.repository;

import mcm.app.entity.Order;
import mcm.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    // First batch: latest 10 orders
    List<Order> findTop10ByOrderByIdDesc();

    // Subsequent batches: orders older than cursor
    List<Order> findTop10ByIdLessThanOrderByIdDesc(Long id);
}