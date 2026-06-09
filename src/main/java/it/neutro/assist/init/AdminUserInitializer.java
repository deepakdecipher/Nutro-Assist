package it.neutro.assist.init;

import com.usermanagement.modelentity.Role;
import com.usermanagement.modelentity.User;
import com.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds the default SUPER_ADMIN user on first startup.
 *
 * Credentials: username=admin / password=admin
 * The user is pre-verified so it can log in without OTP.
 *
 * Runs after JPA schema creation (ApplicationRunner fires post-context-refresh).
 * Safe to run on every startup — does nothing if the admin user already exists.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUserName("admin")) {
            log.debug("Admin user already exists — skipping seed.");
            return;
        }

        Role superAdminRole = Role.builder()
                .roleName("SUPER_ADMIN")
                .build();

        User admin = User.builder()
                .userFullName("Administrator")
                .userName("admin")
                .email("admin@nutro-trust.com")
                .password(passwordEncoder.encode("admin@123"))
                .verified(true)
                .roles(Set.of(superAdminRole))
                .build();

        userRepository.save(admin);
        log.info("Default admin user created (username=admin). Change the password after first login.");
    }
}
