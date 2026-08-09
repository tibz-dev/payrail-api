package com.payrail.apikey;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchantId(UUID merchantId);

    List<ApiKey> findByKeyPrefixAndStatus(String keyPrefix, ApiKeyStatus status);

    Optional<ApiKey> findByIdAndMerchantId(UUID id, UUID merchantId);
}