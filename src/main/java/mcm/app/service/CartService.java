package mcm.app.service;

import mcm.app.dto.CartItemResponseDTO;
import mcm.app.dto.CartResponseDTO;
import mcm.app.dto.ProductResponse;
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
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
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
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }

    // Utility: map to DTO with full product details
    private CartResponseDTO mapToDto(Cart cart) {
        return CartResponseDTO.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .totalPrice(cart.getTotalPrice())
                .items(cart.getItems().stream().map(item -> {
                    ProductResponse productResponse = new ProductResponse();
                    Product product = item.getProduct();
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
                            .quantity(item.getQuantity())
                            .price(product.getPrice())
                            .subTotal(product.getPrice()
                                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build();
                }).toList())
                .build();
    }
}