package com.payrail.ledger;

import com.payrail.merchant.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, UUID> {
    Optional<LedgerAccount> findByMerchantIsNullAndAccountTypeAndCurrency(LedgerAccountType accountType, String currency);
    Optional<LedgerAccount> findByMerchantAndAccountTypeAndCurrency(Merchant merchant, LedgerAccountType accountType, String currency);
}