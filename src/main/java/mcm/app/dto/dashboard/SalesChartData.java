package mcm.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SalesChartData {
    private String date;
    private BigDecimal revenue;
    private Long orderCount;
}
