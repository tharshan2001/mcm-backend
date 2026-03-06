package mcm.app.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDTO {
    private ProductResponse product;  // full product details
    private Integer quantity;
    private BigDecimal price;         // unit price
    private BigDecimal subTotal;      // price * quantity
}