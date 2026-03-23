package mcm.app.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private String username;
    private String shippingAddress;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private String couponCode;
    private LocalDateTime orderDate;
    private List<CartItemResponseDTO> items;
    private AddressResponseDTO address;
}