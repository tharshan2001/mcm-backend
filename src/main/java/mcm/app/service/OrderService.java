package mcm.app.service;

import mcm.app.dto.CartItemResponseDTO;
import mcm.app.dto.OrderResponseDTO;
import mcm.app.dto.ProductResponse;
import mcm.app.dto.AddressResponseDTO;
import mcm.app.entity.*;
import mcm.app.repository.AddressRepository;
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

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    @Transactional
    public Order placeOrder(User user, Long addressId, String couponCode) {
        user = userService.getUserByIdWithUsedCoupons(user.getId());
        
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to the user");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address.getAddressLine());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PLACED");
        order.setPaymentStatus("PAID");

        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Coupon appliedCoupon = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isEmpty()) {
            appliedCoupon = couponService.getCouponByCode(couponCode);
            
            if (userService.hasUserUsedCoupon(user.getId(), appliedCoupon.getId())) {
                throw new RuntimeException("You have already used this coupon");
            }
            
            if (!couponService.isCouponValid(appliedCoupon, total)) {
                throw new RuntimeException("Coupon is not valid or expired");
            }

            discountAmount = calculateDiscount(appliedCoupon, total);
            total = total.subtract(discountAmount);
            order.setTotalPrice(total);
            order.setCoupon(appliedCoupon);
            order.setDiscountAmount(discountAmount);

            couponService.incrementUsage(appliedCoupon.getId());

            if (user.getUsedCoupons() == null) {
                user.setUsedCoupons(new java.util.HashSet<>());
            }
            user.getUsedCoupons().add(appliedCoupon);
            userService.saveUser(user);
        } else {
            order.setTotalPrice(total);
        }

        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            order.getItems().add(orderItem);
        });

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return savedOrder;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            return orderTotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        } else if ("FIXED".equals(coupon.getDiscountType())) {
            return coupon.getDiscountValue().min(orderTotal);
        }
        return BigDecimal.ZERO;
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
     * Convert Order entity to OrderResponseDTO with full product details.
     */
    public OrderResponseDTO toOrderResponseDTO(Order order) {
        List<CartItemResponseDTO> items = order.getItems().stream()
                .map(orderItem -> {
                    Product product = orderItem.getProduct();
                    ProductResponse productResponse = new ProductResponse();
                    productResponse.setId(product.getId());
                    productResponse.setName(product.getName());
                    productResponse.setSlug(product.getSlug());
                    productResponse.setDescription(product.getDescription());
                    productResponse.setPrice(product.getPrice());
                    productResponse.setStockQuantity(product.getStockQuantity());
                    productResponse.setArchived(product.getArchived());
                    productResponse.setCategoryId(product.getCategory().getId());
                    productResponse.setCategoryName(product.getCategory().getName());
                    productResponse.setImages(product.getImages().stream()
                            .map(img -> img.getImageUrl())
                            .toList());

                    return CartItemResponseDTO.builder()
                            .product(productResponse)
                            .quantity(orderItem.getQuantity())
                            .price(product.getPrice()) // unit price
                            .subTotal(product.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .username(order.getUser().getFullName())
                .shippingAddress(order.getShippingAddress())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalPrice(order.getTotalPrice())
                .discountAmount(order.getDiscountAmount())
                .couponCode(order.getCoupon() != null ? order.getCoupon().getCode() : null)
                .orderDate(order.getOrderDate())
                .items(items)
                .build();
    }

    public Order getOrderById(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Ensure the order belongs to the logged-in user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to view this order");
        }

        return order;
    }


    public List<Order> getOrdersForInfiniteScroll(Long cursor) {
        if (cursor == null) {
            // First batch: latest 10 orders
            return orderRepository.findTop10ByOrderByIdDesc();
        } else {
            // Subsequent batches: older orders (id < cursor)
            return orderRepository.findTop10ByIdLessThanOrderByIdDesc(cursor);
        }
    }


    public Order getOrderByIdAdmin(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}