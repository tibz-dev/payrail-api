package com.payrail.provider.mock;

import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import com.payrail.payment.Payment;
import com.payrail.payment.PaymentRepository;
import com.payrail.payment.PaymentStateMachine;
import com.payrail.payment.PaymentStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/mock-provider")
@Profile({"default", "dev", "test"})
public class MockProviderController {

    private final PaymentRepository paymentRepository;
    private final PaymentStateMachine paymentStateMachine;

    public MockProviderController(PaymentRepository paymentRepository, PaymentStateMachine paymentStateMachine) {
        this.paymentRepository = paymentRepository;
        this.paymentStateMachine = paymentStateMachine;
    }

    public record ForceOutcomeRequest(String outcome) {}

    @PostMapping("/{paymentId}/force")
    public ResponseEntity<Void> forceOutcome(@PathVariable String paymentId, @RequestBody ForceOutcomeRequest request) {
        Payment payment = paymentRepository.findByPaymentRef(paymentId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Payment not found."));

        PaymentStatus target = switch (request.outcome().toUpperCase()) {
            case "SUCCESS" -> PaymentStatus.SUCCESS;
            case "FAILED" -> PaymentStatus.FAILED;
            case "EXPIRE" -> PaymentStatus.EXPIRED;
            default -> throw new ApiException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST,
                    "Unknown outcome: " + request.outcome());
        };

        paymentStateMachine.transition(payment, target);
        return ResponseEntity.accepted().build();
    }
}