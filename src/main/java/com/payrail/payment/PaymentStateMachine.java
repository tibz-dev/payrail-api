package com.payrail.payment;

import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PaymentStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = Map.of(
            PaymentStatus.PENDING,    Set.of(PaymentStatus.PROCESSING, PaymentStatus.CANCELLED, PaymentStatus.EXPIRED),
            PaymentStatus.PROCESSING, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED),
            PaymentStatus.SUCCESS,    Set.of(),
            PaymentStatus.FAILED,     Set.of(),
            PaymentStatus.CANCELLED,  Set.of(),
            PaymentStatus.EXPIRED,    Set.of()
    );

    private final PaymentEventRepository paymentEventRepository;

    public PaymentStateMachine(PaymentEventRepository paymentEventRepository) {
        this.paymentEventRepository = paymentEventRepository;
    }

    public void transition(Payment payment, PaymentStatus target) {
        PaymentStatus current = payment.getStatus();

        if (!ALLOWED.getOrDefault(current, Set.of()).contains(target)) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION, HttpStatus.CONFLICT,
                    "Cannot move payment from " + current + " to " + target);
        }

        payment.setStatusInternal(target);
        paymentEventRepository.save(new PaymentEvent(payment, current, target));

        //TODO: Ledger posting and webhook dispatch hook in here once those modules exist.
    }
}