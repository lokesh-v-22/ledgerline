package com.ledgerline.ledgerline.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByAccountType(AccountType type);
    List<Account> findByOwnerIdAndAccountType(UUID ownerId, AccountType type);
    Optional<Account> findFirstByAccountType(AccountType type);
}
