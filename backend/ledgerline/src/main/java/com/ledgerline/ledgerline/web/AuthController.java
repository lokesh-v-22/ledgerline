package com.ledgerline.ledgerline.web;

import com.ledgerline.ledgerline.security.JwtService;
import com.ledgerline.ledgerline.user.Role;
import com.ledgerline.ledgerline.user.User;
import com.ledgerline.ledgerline.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(@NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, max = 100) String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record TokenResponse(String token, String role) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }
        // Role is ALWAYS holder here. The request cannot choose its own role.
        User user = new User(email, encoder.encode(req.password()), Role.HOLDER);
        try {
            users.save(user);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TokenResponse(jwtService.createToken(user), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Optional<User> found = users.findByEmail(req.email().trim().toLowerCase());
        if (found.isEmpty() || !encoder.matches(req.password(), found.get().getPasswordHash())) {
            // Same message for "no such user" and "wrong password".
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }
        User user = found.get();
        return ResponseEntity.ok(new TokenResponse(jwtService.createToken(user), user.getRole().name()));
    }
}