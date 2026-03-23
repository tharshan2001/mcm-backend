package mcm.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopCategory {
    private Long categoryId;
    private String categoryName;
    private Long ordersCount;
    private Long productsCount;
}
