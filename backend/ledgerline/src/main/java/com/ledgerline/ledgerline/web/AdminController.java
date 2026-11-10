package com.ledgerline.ledgerline.web;

import com.ledgerline.ledgerline.audit.AuditService;
import com.ledgerline.ledgerline.security.CurrentUser;
import com.ledgerline.ledgerline.user.Role;
import com.ledgerline.ledgerline.user.User;
import com.ledgerline.ledgerline.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    public record CreateUserRequest(@NotBlank @Email String email,
                                    @NotBlank @Size(min = 8, max = 100) String password,
                                    @NotNull Role role) {}

    public record UserResponse(UUID id, String email, Role role) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public AdminController(UserRepository users, PasswordEncoder encoder, AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.audit = audit;
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest req, Authentication auth) {
        String email = req.email().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }
        User user = users.save(new User(email, encoder.encode(req.password()), req.role()));
        audit.record(CurrentUser.id(auth), "USER_CREATED", "user", user.getId().toString(), "role=" + user.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(user.getId(), user.getEmail(), user.getRole()));
    }
}
