package com.ledgerline.ledgerline.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "balance_paise", nullable = false)
    private long balancePaise;

    protected Account() {
    }

    public Account(UUID ownerId, AccountType accountType) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.accountType = accountType;
        this.balancePaise = 0;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public AccountType getAccountType() { return accountType; }
    public long getBalancePaise() { return balancePaise; }
}
