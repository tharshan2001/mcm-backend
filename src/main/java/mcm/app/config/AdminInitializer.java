package mcm.app.config;

import mcm.app.entity.Role;
import mcm.app.entity.User;
import mcm.app.repository.RoleRepository;
import mcm.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createAdmin() {
        // Only create admin if not exists
        if (userRepository.findByEmail("admin@mcm.com").isEmpty()) {
            // Ensure the admin role exists
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ADMIN");
                        Role saved = roleRepository.save(role);
                        logger.info("ROLE_ADMIN created by AdminInitializer");
                        return saved;
                    });

            // Create admin user
            User admin = new User();
            admin.setFullName("Super Admin");
            admin.setEmail("admin@mcm.com");
            admin.setPassword(passwordEncoder.encode("Admin@123")); // default password
            admin.setRoles(Collections.singleton(adminRole));

            userRepository.save(admin);
            logger.info("Default admin created: admin@mcm.com / Admin@123");
        }
    }
}