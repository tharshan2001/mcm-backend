package mcm.app.service;

import mcm.app.entity.Address;
import mcm.app.entity.User;
import mcm.app.repository.AddressRepository;
import mcm.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public List<Address> getAllAddressesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return addressRepository.findByUser(user);
    }

    public Address addAddress(Long userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        address.setUser(user);
        return addressRepository.save(address);
    }

    public Address updateAddress(Long addressId, Address updatedAddress) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setFullName(updatedAddress.getFullName());
        address.setPhone(updatedAddress.getPhone());
        address.setCountry(updatedAddress.getCountry());
        address.setState(updatedAddress.getState());
        address.setCity(updatedAddress.getCity());
        address.setPostalCode(updatedAddress.getPostalCode());
        address.setAddressLine(updatedAddress.getAddressLine());
        address.setIsDefault(updatedAddress.getIsDefault());

        return addressRepository.save(address);
    }

    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        addressRepository.delete(address);
    }

    public Optional<Address> getAddressById(Long addressId) {
        return addressRepository.findById(addressId);
    }
}