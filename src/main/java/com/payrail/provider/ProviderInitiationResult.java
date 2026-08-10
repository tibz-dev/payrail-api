package com.payrail.provider;

public record ProviderInitiationResult(String providerTransactionId, ProviderStatus initialStatus) {}