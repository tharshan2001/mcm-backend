package mcm.app.service;

import mcm.app.dto.CartItemResponseDTO;
import mcm.app.dto.CartResponseDTO;
import mcm.app.entity.Cart;
import mcm.app.entity.CartItem;
import mcm.app.entity.Product;
import mcm.app.entity.User;
import mcm.app.repository.CartItemRepository;
import mcm.app.repository.CartRepository;
import mcm.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    // Add product to cart
    public CartResponseDTO addToCart(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        if (cart.getItems() == null) cart.setItems(new java.util.HashSet<>());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cart.getItems().add(newItem);
        }

        recalcTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    // Increase quantity
    public CartResponseDTO increaseItem(User user, Long productId) {
        return changeItemQuantity(user, productId, 1);
    }

    // Decrease quantity
    public CartResponseDTO decreaseItem(User user, Long productId) {
        return changeItemQuantity(user, productId, -1);
    }

    private CartResponseDTO changeItemQuantity(User user, Long productId, int delta) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        int newQuantity = item.getQuantity() + delta;

        if (newQuantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(newQuantity);
            item.setPrice(item.getProduct().getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        }

        recalcTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    // Get user's cart
    public CartResponseDTO getCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        if (cart.getItems() == null) cart.setItems(new java.util.HashSet<>());
        recalcTotal(cart);

        return mapToDto(cart);
    }

    // Utility: recalc total price
    private void recalcTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }

    // Utility: map to DTO
    private CartResponseDTO mapToDto(Cart cart) {
        return CartResponseDTO.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .totalPrice(cart.getTotalPrice())
                .items(cart.getItems().stream().map(item ->
                        CartItemResponseDTO.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .subTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}