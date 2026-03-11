package mcm.app.service;

import mcm.app.entity.User;
import mcm.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get customers with cursor-based pagination (infinite scroll)
     *
     * @param cursor ID of the last user from previous page (optional)
     * @param limit  number of users per page
     * @return list of customer users
     */
    public List<User> getCustomersForInfiniteScroll(Long cursor, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);

        List<User> customers;
        if (cursor == null) {
            // First page: get latest users (highest ID first)
            customers = userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(r -> r.getName().equals("CUSTOMER")))
                    .sorted((a, b) -> b.getId().compareTo(a.getId())) // descending by ID
                    .limit(limit)
                    .collect(Collectors.toList());
        } else {
            // Next page: users with ID < cursor
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
}