package mcm.app.service;

import mcm.app.dto.AddressRequestDTO;
import mcm.app.dto.AddressResponseDTO;
import mcm.app.entity.Address;
import mcm.app.entity.User;
import mcm.app.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressResponseDTO createAddress(User user, AddressRequestDTO dto) {

        List<Address> existingAddresses = addressRepository.findByUser(user);
        boolean isFirstAddress = existingAddresses.isEmpty();

        Address address = new Address();
        address.setUser(user);
        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setCountry(dto.getCountry());
        address.setState(dto.getState());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());
        address.setAddressLine(dto.getAddressLine());
        
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            for (Address addr : existingAddresses) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
            address.setIsDefault(true);
        } else {
            address.setIsDefault(isFirstAddress);
        }

        Address saved = addressRepository.save(address);

        return mapToDTO(saved);
    }

    public List<AddressResponseDTO> getUserAddresses(User user) {
        return addressRepository.findByUser(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AddressResponseDTO updateAddress(User user, Long addressId, AddressRequestDTO dto) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to modify this address");
        }

        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            List<Address> existingAddresses = addressRepository.findByUser(user);
            for (Address addr : existingAddresses) {
                if (!addr.getId().equals(addressId)) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            }
            address.setIsDefault(true);
        }

        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setCountry(dto.getCountry());
        address.setState(dto.getState());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());
        address.setAddressLine(dto.getAddressLine());

        Address updated = addressRepository.save(address);

        return mapToDTO(updated);
    }

    public void deleteAddress(User user, Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to delete this address");
        }

        addressRepository.delete(address);
    }

    private AddressResponseDTO mapToDTO(Address address) {
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