package mcm.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Long totalCustomers;
    private Long totalProducts;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private SalesData todaySales;
    private SalesData weekSales;
    private SalesData monthSales;
    private List<TopProduct> topProducts;
    private List<TopCategory> topCategories;
    private List<RecentOrder> recentOrders;
    private List<SalesChartData> revenueChart;
    private List<SalesChartData> ordersChart;
}
