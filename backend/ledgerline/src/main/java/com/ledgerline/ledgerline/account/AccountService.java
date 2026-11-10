package com.ledgerline.ledgerline.account;

import com.ledgerline.ledgerline.audit.AuditService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

    private final AccountRepository accounts;
    private final AuditService audit;

    public AccountService(AccountRepository accounts, AuditService audit) {
        this.accounts = accounts;
        this.audit = audit;
    }

    @Transactional
    public Account openAccount(UUID ownerId) {
        Account account = accounts.save(new Account(ownerId, AccountType.CUSTOMER));
        audit.record(ownerId, "ACCOUNT_OPENED", "account", account.getId().toString(), null);
        return account;
    }

    /** Holders see only their own accounts. Admin and auditor see all customer accounts. The internal SYSTEM account is never shown. */
    @Transactional(readOnly = true)
    public Account getVisible(UUID accountId, UUID viewerId, boolean canSeeAll) {
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        boolean allowed = account.getAccountType() == AccountType.CUSTOMER
                && (canSeeAll || account.getOwnerId().equals(viewerId));
        if (!allowed) {
            // Same answer as "does not exist", so nobody can probe which IDs exist.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return account;
    }

    @Transactional(readOnly = true)
    public List<Account> listVisible(UUID viewerId, boolean canSeeAll) {
        return canSeeAll
                ? accounts.findByAccountType(AccountType.CUSTOMER)
                : accounts.findByOwnerIdAndAccountType(viewerId, AccountType.CUSTOMER);
    }
}
