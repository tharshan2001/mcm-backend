package mcm.app.repository;

import mcm.app.entity.Order;
import mcm.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    // First batch: latest 10 orders
    List<Order> findTop10ByOrderByIdDesc();

    // Subsequent batches: orders older than cursor
    List<Order> findTop10ByIdLessThanOrderByIdDesc(Long id);

    // Dashboard queries
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.orderDate >= :startDate")
    BigDecimal getTotalRevenueSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate")
    Long getOrderCountSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC LIMIT :limit")
    List<Order> findRecentOrders(@Param("limit") int limit);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'CUSTOMER'")
    Long countCustomers();

    @Query(value = "SELECT SUM(oi.quantity) FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE o.order_date >= :startDate", nativeQuery = true)
    Long getTotalQuantitySoldSince(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT p.id, p.name, COALESCE(SUM(oi.quantity), 0) as total_qty, COUNT(DISTINCT o.id) as order_count " +
            "FROM product p " +
            "JOIN order_item oi ON oi.product_id = p.id " +
            "JOIN orders o ON o.id = oi.order_id " +
            "WHERE o.order_date >= :startDate " +
            "GROUP BY p.id, p.name " +
            "ORDER BY total_qty DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopProductsSince(@Param("startDate") LocalDateTime startDate, @Param("limit") int limit);

    @Query("SELECT FUNCTION('DATE', o.orderDate) as orderDate, SUM(o.totalPrice) as revenue, COUNT(o) as orderCount " +
            "FROM Order o " +
            "WHERE o.orderDate >= :startDate " +
            "GROUP BY FUNCTION('DATE', o.orderDate) " +
            "ORDER BY orderDate")
    List<Object[]> getSalesChartData(@Param("startDate") LocalDateTime startDate);
}