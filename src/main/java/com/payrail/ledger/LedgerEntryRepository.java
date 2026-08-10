package com.payrail.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByPaymentId(UUID paymentId);

    // Deliberately no update/delete methods exposed — append-only is enforced
    // at the repository interface level, not just by convention.
}