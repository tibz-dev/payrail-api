package com.payrail.payment.dto;

import java.time.Instant;

public record PaymentResponse(
        String id,
        String status,
        long amount,
        String currency,
        String reference,
        String checkoutUrl,
        Instant expiresAt,
        Instant createdAt
) {}