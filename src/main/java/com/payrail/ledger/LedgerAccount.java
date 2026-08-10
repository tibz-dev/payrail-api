package com.payrail.ledger;

import com.payrail.merchant.Merchant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_accounts")
@Getter
@NoArgsConstructor
public class LedgerAccount {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant; // null for platform-level accounts (e.g. CLEARING)

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private LedgerAccountType accountType;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public LedgerAccount(Merchant merchant, LedgerAccountType accountType, String currency) {
        this.merchant = merchant;
        this.accountType = accountType;
        this.currency = currency;
        this.createdAt = Instant.now();
    }
}