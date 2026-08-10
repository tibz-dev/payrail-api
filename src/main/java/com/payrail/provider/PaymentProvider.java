package com.payrail.provider;

import com.payrail.payment.Payment;

public interface PaymentProvider {

    ProviderInitiationResult initiate(Payment payment, String method);

    ProviderStatus checkStatus(String providerTransactionId);

    String providerName();
}