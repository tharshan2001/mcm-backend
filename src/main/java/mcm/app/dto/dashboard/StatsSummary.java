package mcm.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class StatsSummary {
    private Long totalCustomers;
    private Long totalProducts;
    private Long totalOrders;
    private BigDecimal totalRevenue;
}
