package mcm.app.controller;

import mcm.app.dto.dashboard.*;
import mcm.app.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<StatsSummary> getStatsSummary() {
        return ResponseEntity.ok(dashboardService.getStatsSummary());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sales")
    public ResponseEntity<SalesPeriodData> getSalesData(
            @RequestParam(defaultValue = "month") String period) {
        return ResponseEntity.ok(dashboardService.getSalesData(period));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue-chart")
    public ResponseEntity<List<SalesChartData>> getRevenueChart(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getRevenueChart(days));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders-chart")
    public ResponseEntity<List<SalesChartData>> getOrdersChart(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getOrdersChart(days));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProduct>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getTopProducts(limit));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recent-orders")
    public ResponseEntity<List<RecentOrder>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(limit));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top-categories")
    public ResponseEntity<List<TopCategory>> getTopCategories(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getTopCategories(limit));
    }
}
