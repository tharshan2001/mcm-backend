package mcm.app.service;

import mcm.app.dto.dashboard.*;
import mcm.app.entity.Order;
import mcm.app.repository.OrderRepository;
import mcm.app.repository.ProductRepository;
import mcm.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public StatsSummary getStatsSummary() {
        Long totalCustomers = userRepository.countCustomers();
        Long totalProducts = productRepository.countActiveProducts();
        Long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.getTotalRevenueSince(LocalDateTime.of(1970, 1, 1, 0, 0));

        return StatsSummary.builder()
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .build();
    }

    public SalesPeriodData getSalesData(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = todayStart.minusDays(7);
        LocalDateTime monthStart = todayStart.minusDays(30);

        SalesData today = calculateSalesData(todayStart);
        SalesData week = calculateSalesData(weekStart);
        SalesData month = calculateSalesData(monthStart);

        return SalesPeriodData.builder()
                .today(today)
                .week(week)
                .month(month)
                .build();
    }

    public List<SalesChartData> getRevenueChart(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = orderRepository.getSalesChartData(startDate);
        
        return results.stream()
                .map(row -> {
                    String dateStr;
                    if (row[0] instanceof java.sql.Timestamp) {
                        dateStr = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate().toString();
                    } else if (row[0] instanceof java.sql.Date) {
                        dateStr = ((java.sql.Date) row[0]).toLocalDate().toString();
                    } else {
                        dateStr = row[0].toString();
                    }
                    return SalesChartData.builder()
                            .date(dateStr)
                            .revenue((BigDecimal) row[1])
                            .orderCount(((Long) row[2]))
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<SalesChartData> getOrdersChart(int days) {
        return getRevenueChart(days);
    }

    public List<TopProduct> getTopProducts(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> results = orderRepository.getTopProductsSince(since, limit);
        
        return results.stream()
                .map(row -> TopProduct.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .quantitySold(((Number) row[2]).longValue())
                        .orderCount(((Number) row[3]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<RecentOrder> getRecentOrders(int limit) {
        return orderRepository.findRecentOrders(limit).stream()
                .map(order -> RecentOrder.builder()
                        .orderId(order.getId())
                        .customerName(order.getUser().getFullName())
                        .totalPrice(order.getTotalPrice())
                        .orderStatus(order.getOrderStatus())
                        .orderDate(order.getOrderDate())
                        .build())
                .collect(Collectors.toList());
    }

    public List<TopCategory> getTopCategories(int limit) {
        return List.of();
    }

    private SalesData calculateSalesData(LocalDateTime startDate) {
        BigDecimal revenue = orderRepository.getTotalRevenueSince(startDate);
        Long orders = orderRepository.getOrderCountSince(startDate);

        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (orders != null && orders > 0 && revenue != null) {
            avgOrderValue = revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
        }

        return SalesData.builder()
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .totalOrders(orders != null ? orders : 0L)
                .averageOrderValue(avgOrderValue)
                .build();
    }
}
