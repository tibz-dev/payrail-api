package com.payrail.apikey;

import com.payrail.merchant.Merchant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "key_prefix", nullable = false)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false)
    private String keyHash;

    @Column(name = "last_four", nullable = false)
    private String lastFour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiKeyStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public ApiKey(Merchant merchant, String keyPrefix, String keyHash, String lastFour) {
        this.merchant = merchant;
        this.keyPrefix = keyPrefix;
        this.keyHash = keyHash;
        this.lastFour = lastFour;
        this.status = ApiKeyStatus.ACTIVE;
        this.createdAt = Instant.now();
    }
}