package mcm.app.controller;

import mcm.app.dto.CouponResponse;
import mcm.app.dto.UserResponseDTO;
import mcm.app.entity.Coupon;
import mcm.app.entity.User;
import mcm.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/scroll")
    public ResponseEntity<List<UserResponseDTO>> getCustomersForScroll(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<User> customers = userService.getCustomersForInfiniteScroll(cursor, limit);

        List<UserResponseDTO> response = customers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        if (user.getUsedCoupons() != null) {
            dto.setUsedCoupons(user.getUsedCoupons().stream()
                    .map(this::mapCouponToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private CouponResponse mapCouponToDTO(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setDiscountType(coupon.getDiscountType());
        response.setDiscountValue(coupon.getDiscountValue());
        response.setMinOrderAmount(coupon.getMinOrderAmount());
        response.setMaxUsage(coupon.getMaxUsage());
        response.setUsedCount(coupon.getUsedCount());
        response.setExpiryDate(coupon.getExpiryDate());
        response.setActive(coupon.isActive());
        response.setDescription(coupon.getDescription());
        return response;
    }
}