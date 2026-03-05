package mcm.app.controller;

import mcm.app.dto.AddressRequestDTO;
import mcm.app.dto.AddressResponseDTO;
import mcm.app.entity.User;
import mcm.app.security.CustomUserDetails;
import mcm.app.service.AddressService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<?> createAddress(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody AddressRequestDTO dto) {

        try {

            User user = principal.getUser();

            AddressResponseDTO response = addressService.createAddress(user, dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<?> getAddresses(
            @AuthenticationPrincipal CustomUserDetails principal) {

        try {

            User user = principal.getUser();

            List<AddressResponseDTO> addresses =
                    addressService.getUserAddresses(user);

            return ResponseEntity.ok(addresses);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch addresses"));
        }
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long addressId,
            @RequestBody AddressRequestDTO dto) {

        try {

            User user = principal.getUser();

            AddressResponseDTO updated =
                    addressService.updateAddress(user, addressId, dto);

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long addressId) {

        try {

            User user = principal.getUser();

            addressService.deleteAddress(user, addressId);

            return ResponseEntity.ok(
                    Map.of("message", "Address deleted successfully")
            );

        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}