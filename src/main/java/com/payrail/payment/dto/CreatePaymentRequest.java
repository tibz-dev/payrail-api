package com.payrail.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
        @Positive(message = "Amount must be greater than zero") long amount,
        @NotBlank String currency,
        @NotBlank String reference,
        String description
) {}