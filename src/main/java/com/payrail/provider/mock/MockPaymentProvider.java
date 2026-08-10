package com.payrail.provider.mock;

import com.payrail.payment.Payment;
import com.payrail.provider.PaymentProvider;
import com.payrail.provider.ProviderInitiationResult;
import com.payrail.provider.ProviderStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile({"default", "dev", "test"})
public class MockPaymentProvider implements PaymentProvider {

    // In-memory only — MVP scope, no persistence needed for simulated provider state.
    private final Map<String, ProviderStatus> outcomes = new ConcurrentHashMap<>();

    @Override
    public ProviderInitiationResult initiate(Payment payment, String method) {
        String providerTxId = "mock_" + UUID.randomUUID();
        outcomes.put(providerTxId, ProviderStatus.PROCESSING);
        return new ProviderInitiationResult(providerTxId, ProviderStatus.PROCESSING);
    }

    @Override
    public ProviderStatus checkStatus(String providerTransactionId) {
        return outcomes.getOrDefault(providerTransactionId, ProviderStatus.PROCESSING);
    }

    @Override
    public String providerName() {
        return "MOCK";
    }

    /** Test-control hook — forces a terminal outcome for a given provider transaction. */
    public void forceOutcome(String providerTransactionId, ProviderStatus outcome) {
        outcomes.put(providerTransactionId, outcome);
    }
}