package mcm.app.controller;

import com.stripe.model.PaymentIntent;
import mcm.app.dto.OrderResponseDTO;
import mcm.app.entity.Order;
import mcm.app.entity.User;
import mcm.app.security.CustomUserDetails;
import mcm.app.service.OrderService;
import mcm.app.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    // Step 1: Create PaymentIntent
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

    // Step 2: Confirm payment and place order using addressId
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutOrder(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam Long addressId,
            @RequestParam String paymentIntentId) {

        User user = principal.getUser();

        try {
            paymentService.markPaymentSuccess(paymentIntentId);

            // Place order using internal address fetch
            Order order = orderService.placeOrder(user, addressId);

            OrderResponseDTO dto = orderService.toOrderResponseDTO(order);
            return ResponseEntity.ok(dto);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Something went wrong. Please try again."));
        }
    }

    // Get orders of authenticated user
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDTO>> myOrders(
            @AuthenticationPrincipal CustomUserDetails principal) {

        User user = principal.getUser();
        List<OrderResponseDTO> orders = orderService.getOrdersByUser(user)
                .stream()
                .map(orderService::toOrderResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }

    // Admin updates order status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(orderService.toOrderResponseDTO(order));
    }
}