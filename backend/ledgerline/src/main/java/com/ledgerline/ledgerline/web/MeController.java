package com.ledgerline.ledgerline.web;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "userId", authentication.getPrincipal().toString(),
                "roles", authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }
}