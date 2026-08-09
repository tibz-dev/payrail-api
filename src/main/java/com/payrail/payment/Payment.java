package com.payrail.payment;

import com.payrail.merchant.Merchant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_ref", nullable = false, unique = true)
    private String paymentRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String reference;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "checkout_token", nullable = false, unique = true)
    private String checkoutToken;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public Payment(String paymentRef, Merchant merchant, long amountMinor, String currency,
                   String reference, String description, String checkoutToken, Instant expiresAt) {
        this.paymentRef = paymentRef;
        this.merchant = merchant;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.reference = reference;
        this.description = description;
        this.status = PaymentStatus.PENDING;
        this.checkoutToken = checkoutToken;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /** Package-private on purpose — only PaymentStateMachine may call this. */
    void setStatusInternal(PaymentStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}