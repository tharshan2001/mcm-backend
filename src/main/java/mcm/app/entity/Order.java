package mcm.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    private LocalDateTime orderDate = LocalDateTime.now();

    private String orderStatus = "PLACED"; // PLACED | SHIPPED | DELIVERED | CANCELLED

    private BigDecimal totalPrice;

    private String paymentStatus = "PAID"; // PAID | PENDING | FAILED

    private String shippingAddress;

    private String trackingNumber;

    private String returnStatus; // REQUESTED | APPROVED | REJECTED | REFUNDED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderItem> items = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private BigDecimal discountAmount;
}