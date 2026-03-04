package mcm.app.controller;

import mcm.app.dto.*;
import mcm.app.entity.User;
import mcm.app.security.CustomUserDetails;
import mcm.app.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody AddToCartRequest request
    ) {
        User user = principal.getUser();
        return ResponseEntity.ok(cartService.addToCart(user, request.getProductId(), request.getQuantity()));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/increase/{productId}")
    public ResponseEntity<CartResponseDTO> increaseItem(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long productId
    ) {
        User user = principal.getUser();
        return ResponseEntity.ok(cartService.increaseItem(user, productId));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/decrease/{productId}")
    public ResponseEntity<CartResponseDTO> decreaseItem(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long productId
    ) {
        User user = principal.getUser();
        return ResponseEntity.ok(cartService.decreaseItem(user, productId));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/view")
    public ResponseEntity<CartResponseDTO> viewCart(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        User user = principal.getUser();
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/checkout")
    public ResponseEntity<CartResponseDTO> checkout(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        User user = principal.getUser();
        return ResponseEntity.ok(cartService.checkout(user));
    }
}