package it.neutro.assist.admin;

import com.usermanagement.config.CustomUserDetails;
import com.usermanagement.firebase.FirebaseTokenVerifier;
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
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    public UnifiedOtpAuthController(
            UserRepository userRepository,
            UserService userService,
            GenerateOtp generateOtp,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            FirebaseTokenVerifier firebaseTokenVerifier) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.generateOtp = generateOtp;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.firebaseTokenVerifier = firebaseTokenVerifier;
    }

    // ── Email OTP flow ────────────────────────────────────────────────────────

    /**
     * Sends an OTP to the given email. Auto-creates the user if they don't exist yet
     * (passwordless sign-up + sign-in unified into one flow).
     */
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

    /**
     * Validates the email OTP and returns a JWT. Marks the user as verified on first login.
     */
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

        return ResponseEntity.ok(buildJwtResponse(user));
    }

    // ── Phone OTP flow (Firebase) ─────────────────────────────────────────────

    /**
     * Exchanges a Firebase phone-auth ID token for an app JWT.
     *
     * The client (FE) drives the SMS OTP flow via the Firebase JS SDK:
     *   1. {@code signInWithPhoneNumber(auth, phone, recaptchaVerifier)} — Firebase sends SMS
     *   2. {@code confirmationResult.confirm(otp)} — validates OTP with Firebase
     *   3. {@code userCredential.user.getIdToken()} — gets the ID token sent here
     *
     * The user is auto-created on first login (phone-only account).
     */
    @PostMapping("/login-phone")
    @Transactional
    public ResponseEntity<JwtResponse> loginWithPhone(@RequestBody PhoneLoginRequest request) {
        String phone = firebaseTokenVerifier.verifyAndGetPhoneNumber(request.idToken());

        User user = userRepository.findByPhoneNumber(phone)
                .orElseGet(() -> createPhoneUser(phone));

        user.setVerified(true);
        user.setPhoneNumber(phone);
        userRepository.save(user);

        return ResponseEntity.ok(buildJwtResponse(user));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User createPhoneUser(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        String last4 = digits.length() >= 4 ? digits.substring(digits.length() - 4) : digits;

        String baseUserName = "phone_" + digits;
        String candidate = baseUserName;
        int suffix = 1;
        while (userRepository.existsByUserName(candidate)) {
            candidate = baseUserName + "_" + suffix++;
        }

        // Phone-only users get a synthetic internal email (never shown to the user)
        String syntheticEmail = "phone_" + digits + "@nutro-assist.phone";

        User user = User.builder()
                .userFullName("User " + last4)
                .userName(candidate)
                .email(syntheticEmail)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(UUID.randomUUID().toString() + "Aa1!"))
                .verified(true)
                .roles(Set.of(Role.builder().roleName("USER").build()))
                .build();
        return userRepository.save(user);
    }

    private JwtResponse buildJwtResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        return new JwtResponse(token, refreshToken, roles);
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
