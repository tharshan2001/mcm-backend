package mcm.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SalesPeriodData {
    private SalesData today;
    private SalesData week;
    private SalesData month;
}
