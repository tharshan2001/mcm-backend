package mcm.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cart")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    private BigDecimal totalPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy="cart", cascade=CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();
}