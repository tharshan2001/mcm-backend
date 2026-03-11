package mcm.app.controller;

import mcm.app.dto.UserResponseDTO;
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

    /**
     * Admin can get customers with infinite scroll
     *
     * @param cursor optional userId of the last user from previous page
     * @param limit  number of users to return
     */
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

    // Mapper
    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }
}