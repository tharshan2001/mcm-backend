package mcm.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "state")
    private String state;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address_line", columnDefinition = "TEXT", nullable = false)
    private String addressLine;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}