package mcm.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentOrder {
    private Long orderId;
    private String customerName;
    private BigDecimal totalPrice;
    private String orderStatus;
    private LocalDateTime orderDate;
}
