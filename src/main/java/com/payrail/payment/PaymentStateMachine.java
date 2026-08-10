package com.payrail.payment;

import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import com.payrail.ledger.LedgerService;
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
    private final LedgerService ledgerService;

    public PaymentStateMachine(PaymentEventRepository paymentEventRepository, LedgerService ledgerService) {
        this.paymentEventRepository = paymentEventRepository;
        this.ledgerService = ledgerService;
    }

    public void transition(Payment payment, PaymentStatus target) {
        PaymentStatus current = payment.getStatus();

        if (!ALLOWED.getOrDefault(current, Set.of()).contains(target)) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION, HttpStatus.CONFLICT,
                    "Cannot move payment from " + current + " to " + target);
        }

        payment.setStatusInternal(target);
        paymentEventRepository.save(new PaymentEvent(payment, current, target));

        if (target == PaymentStatus.SUCCESS) {
            ledgerService.postPaymentSuccess(payment);
        }

        //TODO: Webhook dispatch hooks in here next step.
    }
}