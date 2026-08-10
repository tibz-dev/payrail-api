package com.payrail.payment;

public record PaymentCreationResult(Payment payment, boolean replayed) {}