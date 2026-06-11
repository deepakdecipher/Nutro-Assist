package it.neutro.assist.shared;

import com.usermanagement.modelentity.User;
import com.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIncludingUnverified(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
