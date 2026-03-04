package mcm.app.controller;

import com.stripe.model.PaymentIntent;
import mcm.app.entity.Order;
import mcm.app.entity.User;
import mcm.app.security.CustomUserDetails;
import mcm.app.service.OrderService;
import mcm.app.service.PaymentService;
import mcm.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    // Step 1: Create PaymentIntent for authenticated user
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/pay")
    public ResponseEntity<Map<String, String>> createPayment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam BigDecimal amount) throws Exception {

        User user = principal.getUser();

        PaymentIntent intent = paymentService.createPaymentIntent(user, amount);

        return ResponseEntity.ok(Map.of(
                "clientSecret", intent.getClientSecret(),
                "paymentId", intent.getId()
        ));
    }

    // Step 2: Confirm payment success and checkout
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkoutOrder(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam String shippingAddress,
            @RequestParam String paymentIntentId) {

        User user = principal.getUser();

        // Mark payment as successful
        paymentService.markPaymentSuccess(paymentIntentId);

        // Place the order and clear cart
        Order order = orderService.placeOrder(user, shippingAddress);
        return ResponseEntity.ok(order);
    }

    // User views own orders
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public ResponseEntity<?> myOrders(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        return ResponseEntity.ok(orderService.getOrdersByUser(user));
    }

    // Admin updates order status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long orderId,
                                              @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}