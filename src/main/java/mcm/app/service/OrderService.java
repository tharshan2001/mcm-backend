package mcm.app.service;

import mcm.app.dto.CartItemResponseDTO;
import mcm.app.dto.OrderResponseDTO;
import mcm.app.entity.*;
import mcm.app.repository.CartRepository;
import mcm.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    /**
     * Place order after payment success and clear cart.
     */
    @Transactional
    public Order placeOrder(User user, String shippingAddress) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PLACED");
        order.setPaymentStatus("PAID");

        // Calculate total
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);

        // Map cart items to order items
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            order.getItems().add(orderItem);
        });

        // Save order (cascade ensures OrderItems are saved)
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return savedOrder;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }

    /**
     * Convert Order entity to OrderResponseDTO
     */
    public OrderResponseDTO toOrderResponseDTO(Order order) {
        List<CartItemResponseDTO> items = order.getItems().stream()
                .map(item -> CartItemResponseDTO.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .shippingAddress(order.getShippingAddress())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getOrderDate())
                .items(items)
                .build();
    }
}