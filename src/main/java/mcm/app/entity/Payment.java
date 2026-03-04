package mcm.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    private BigDecimal amount;

    private String currency = "usd";

    private String stripePaymentId;

    private String status; // SUCCESS | FAILED | PENDING

    private LocalDateTime createdAt = LocalDateTime.now();
}