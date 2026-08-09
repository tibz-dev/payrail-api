package com.payrail.apikey;

import com.payrail.apikey.dto.ApiKeyResponse;
import com.payrail.apikey.dto.ApiKeySummaryResponse;
import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import com.payrail.merchant.Merchant;
import com.payrail.merchant.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "pk_test_";

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, MerchantRepository merchantRepository,
                         PasswordEncoder passwordEncoder) {
        this.apiKeyRepository = apiKeyRepository;
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ApiKeyResponse createKey(String merchantRef) {
        Merchant merchant = merchantRepository.findByMerchantRef(merchantRef)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Merchant not found."));

        StringBuilder raw = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            raw.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        String fullKey = PREFIX + raw;
        String lastFour = raw.substring(raw.length() - 4);
        String keyHash = passwordEncoder.encode(fullKey);

        ApiKey apiKey = new ApiKey(merchant, PREFIX, keyHash, lastFour);
        apiKeyRepository.save(apiKey);

        return new ApiKeyResponse(apiKey.getId().toString(), fullKey, apiKey.getCreatedAt());
    }

    public List<ApiKeySummaryResponse> listKeys(String merchantRef) {
        Merchant merchant = merchantRepository.findByMerchantRef(merchantRef)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Merchant not found."));

        return apiKeyRepository.findByMerchantId(merchant.getId()).stream()
                .map(k -> new ApiKeySummaryResponse(
                        k.getId().toString(), k.getKeyPrefix(), k.getLastFour(), k.getStatus().name(), k.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void revokeKey(String merchantRef, UUID keyId) {
        Merchant merchant = merchantRepository.findByMerchantRef(merchantRef)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Merchant not found."));

        ApiKey apiKey = apiKeyRepository.findByIdAndMerchantId(keyId, merchant.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "API key not found."));

        apiKey.setStatus(ApiKeyStatus.REVOKED);
        apiKey.setRevokedAt(Instant.now());
    }

    /** Used later by ApiKeyAuthFilter to resolve a raw key from a request header. */
    public Merchant resolveMerchant(String rawKey) {
        if (!rawKey.startsWith(PREFIX)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid API key.");
        }
        List<ApiKey> candidates = apiKeyRepository.findByKeyPrefixAndStatus(PREFIX, ApiKeyStatus.ACTIVE);
        return candidates.stream()
                .filter(k -> passwordEncoder.matches(rawKey, k.getKeyHash()))
                .findFirst()
                .map(ApiKey::getMerchant)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid API key."));
    }
}