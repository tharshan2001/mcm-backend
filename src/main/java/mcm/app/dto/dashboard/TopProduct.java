package mcm.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopProduct {
    private Long productId;
    private String productName;
    private Long quantitySold;
    private Long orderCount;
}
