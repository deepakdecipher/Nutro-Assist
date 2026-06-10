package it.neutro.assist.admin;

import com.usermanagement.config.CustomUserDetails;
import com.usermanagement.jwt.JwtUtil;
import com.usermanagement.modelentity.Role;
import com.usermanagement.modelentity.User;
import com.usermanagement.modelrequest.UserSignUp;
import com.usermanagement.modelresponse.JwtResponse;
import com.usermanagement.otp.GenerateOtp;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/userApi/unified")
public class UnifiedOtpAuthController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final GenerateOtp generateOtp;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UnifiedOtpAuthController(
            UserRepository userRepository,
            UserService userService,
            GenerateOtp generateOtp,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.generateOtp = generateOtp;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/generate-otp/{emailId}")
    @Transactional
    public ResponseEntity<String> generateUnifiedOtp(@PathVariable String emailId) {
        String email = normalizeEmail(emailId);
        if (userRepository.findByEmailIncludingUnverified(email).isPresent()) {
            return ResponseEntity.ok(userService.generateOtp(email));
        }

        UserSignUp request = new UserSignUp();
        request.setUserFullName(defaultName(email));
        request.setUserName(uniqueUserName(email));
        request.setEmail(email);
        request.setPassword(UUID.randomUUID().toString() + "Aa1!");
        request.setRole(Set.of(Role.builder().roleName("USER").build()));
        userService.save(request);
        return ResponseEntity.ok("OTP sent to " + email);
    }

    @PostMapping("/login-otp")
    @Transactional
    public ResponseEntity<JwtResponse> loginWithUnifiedOtp(@RequestBody UnifiedOtpLoginRequest request) {
        String email = normalizeEmail(request.email());
        generateOtp.validateOtp(request.otp(), email);

        User user = userRepository.findByEmailIncludingUnverified(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setVerified(true);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString() + "Aa1!"));
        }
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(new JwtResponse(token, refreshToken, roles));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        return email.trim().toLowerCase();
    }

    private String defaultName(String email) {
        String local = email.split("@", 2)[0].replaceAll("[._-]+", " ").trim();
        return local.isBlank() ? "Nutro Assist User" : local;
    }

    private String uniqueUserName(String email) {
        String base = email.split("@", 2)[0].replaceAll("[^a-zA-Z0-9]", "");
        if (base.isBlank()) base = "user";
        String candidate = base;
        int suffix = 1000;
        while (userRepository.existsByUserName(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
