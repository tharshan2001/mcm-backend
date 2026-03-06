package mcm.app.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO {
    private Long cartId;
    private Long userId;
    private BigDecimal totalPrice;           // sum of all subTotals
    private List<CartItemResponseDTO> items;
}