package com.payrail.checkout.dto;

import java.util.List;

public record CheckoutViewResponse(
        String paymentId,
        String merchantName,
        long amount,
        String currency,
        String reference,
        String status,
        List<String> availableMethods
) {}