package mcm.app.controller;

import mcm.app.dto.AddressResponseDTO;
import mcm.app.dto.UserRequest;
import mcm.app.dto.UserResponseDTO;
import mcm.app.entity.Address;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/{id}")
    public ResponseEntity<UserResponseDTO> getCustomerById(@PathVariable Long id) {
        User customer = userService.getCustomerById(id);
        return ResponseEntity.ok(mapToDTO(customer));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/customers/{id}")
    public ResponseEntity<UserResponseDTO> updateCustomer(
            @PathVariable Long id,
            @RequestBody UserRequest request
    ) {
        User updated = userService.updateCustomer(id, request.getFullName(), request.getEmail(), request.getPhoneNumber());
        return ResponseEntity.ok(mapToDTO(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/customers/{id}/deactivate")
    public ResponseEntity<String> deactivateCustomer(@PathVariable Long id) {
        userService.deactivateCustomer(id);
        return ResponseEntity.ok("Customer deactivated successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/customers/{id}/activate")
    public ResponseEntity<String> activateCustomer(@PathVariable Long id) {
        userService.activateCustomer(id);
        return ResponseEntity.ok("Customer activated successfully");
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        if (user.getAddresses() != null) {
            dto.setAddresses(user.getAddresses().stream()
                    .filter(Address::getIsDefault)
                    .map(this::mapAddressToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private AddressResponseDTO mapAddressToDTO(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .country(address.getCountry())
                .state(address.getState())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .addressLine(address.getAddressLine())
                .isDefault(address.getIsDefault())
                .build();
    }
}