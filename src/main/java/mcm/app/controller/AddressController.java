package mcm.app.controller;

import mcm.app.entity.Address;
import mcm.app.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getAddressesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getAllAddressesByUserId(userId));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/user/{userId}")
    public ResponseEntity<Address> createAddress(@PathVariable Long userId, @RequestBody Address address) {
        return ResponseEntity.ok(addressService.addAddress(userId, address));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long addressId, @RequestBody Address address) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, address));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{addressId}")
    public ResponseEntity<Address> getAddress(@PathVariable Long addressId) {
        return addressService.getAddressById(addressId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}