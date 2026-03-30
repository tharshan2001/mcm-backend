package mcm.app.service;

import mcm.app.entity.User;
import mcm.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getCustomersForInfiniteScroll(Long cursor, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);

        List<User> customers;
        if (cursor == null) {
            customers = userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(r -> r.getName().equals("CUSTOMER")))
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
        } else {
            customers = userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(r -> r.getName().equals("CUSTOMER")) && u.getId() < cursor)
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return customers;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public User getUserByIdWithUsedCoupons(Long userId) {
        return userRepository.findByIdWithUsedCoupons(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public boolean hasUserUsedCoupon(Long userId, Long couponId) {
        return userRepository.hasUserUsedCoupon(userId, couponId);
    }

    @Transactional(readOnly = true)
    public User getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (user.getRoles().stream().noneMatch(r -> r.getName().equals("CUSTOMER"))) {
            throw new RuntimeException("User is not a customer");
        }
        return user;
    }

    @Transactional
    public User updateCustomer(Long id, String fullName, String email, String phoneNumber) {
        User user = getCustomerById(id);
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (email != null && !email.isBlank()) {
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(email);
        }
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        User user = getCustomerById(id);
        userRepository.delete(user);
    }
}