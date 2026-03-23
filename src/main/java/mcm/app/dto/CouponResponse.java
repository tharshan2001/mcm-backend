package mcm.app.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponResponse {
    private Long id;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private Integer maxUsage;
    private Integer usedCount;
    private LocalDateTime expiryDate;
    private boolean active;
    private String description;
}