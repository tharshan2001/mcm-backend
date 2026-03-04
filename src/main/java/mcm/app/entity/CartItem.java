package mcm.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_item")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="cart_id", nullable=false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name="product_id", nullable=false)
    private Product product;

    private Integer quantity;

    private BigDecimal price; // price at the time of adding to cart
}