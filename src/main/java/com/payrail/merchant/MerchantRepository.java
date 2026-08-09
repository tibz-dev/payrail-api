package com.payrail.merchant;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByMerchantRef(String merchantRef);
    Optional<Merchant> findByUserId(UUID userId);
}