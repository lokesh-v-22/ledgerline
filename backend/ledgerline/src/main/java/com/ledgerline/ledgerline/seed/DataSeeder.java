package com.ledgerline.ledgerline.seed;

import com.ledgerline.ledgerline.account.Account;
import com.ledgerline.ledgerline.account.AccountRepository;
import com.ledgerline.ledgerline.account.AccountType;
import com.ledgerline.ledgerline.user.Role;
import com.ledgerline.ledgerline.user.User;
import com.ledgerline.ledgerline.user.UserRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final String SYSTEM_EMAIL = "system@ledgerline.internal";

    private final UserRepository users;
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    private final String seedPassword;

    public DataSeeder(UserRepository users, AccountRepository accounts, PasswordEncoder encoder,
                      @Value("${SEED_PASSWORD:}") String seedPassword) {
        this.users = users;
        this.accounts = accounts;
        this.encoder = encoder;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Internal user nobody can log in as: its password is random and never stored anywhere.
        User systemUser = users.findByEmail(SYSTEM_EMAIL).orElseGet(() ->
                users.save(new User(SYSTEM_EMAIL, encoder.encode(UUID.randomUUID().toString()), Role.AUDITOR)));
        if (accounts.findFirstByAccountType(AccountType.SYSTEM).isEmpty()) {
            accounts.save(new Account(systemUser.getId(), AccountType.SYSTEM));
        }

        if (seedPassword == null || seedPassword.isBlank()) {
            return;
        }
        seedUser("admin@ledgerline.test", Role.ADMIN);
        seedUser("auditor@ledgerline.test", Role.AUDITOR);
        seedUser("alice@ledgerline.test", Role.HOLDER);
        seedUser("bob@ledgerline.test", Role.HOLDER);
    }

    private void seedUser(String email, Role role) {
        if (users.findByEmail(email).isPresent()) {
            return;
        }
        User user = users.save(new User(email, encoder.encode(seedPassword), role));
        if (role == Role.HOLDER) {
            accounts.save(new Account(user.getId(), AccountType.CUSTOMER));
        }
    }
}
