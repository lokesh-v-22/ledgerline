package com.ledgerline.ledgerline.web;

import com.ledgerline.ledgerline.account.Account;
import com.ledgerline.ledgerline.account.AccountService;
import com.ledgerline.ledgerline.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    public record AccountResponse(UUID id, UUID ownerId, long balancePaise) {
        static AccountResponse of(Account a) {
            return new AccountResponse(a.getId(), a.getOwnerId(), a.getBalancePaise());
        }
    }

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('HOLDER')")
    public ResponseEntity<AccountResponse> open(Authentication auth) {
        Account account = service.openAccount(CurrentUser.id(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.of(account));
    }

    @GetMapping
    public List<AccountResponse> list(Authentication auth) {
        return service.listVisible(CurrentUser.id(auth), CurrentUser.canSeeAll(auth))
                .stream().map(AccountResponse::of).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id, Authentication auth) {
        return AccountResponse.of(service.getVisible(id, CurrentUser.id(auth), CurrentUser.canSeeAll(auth)));
    }
}
