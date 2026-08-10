package com.payrail.checkout;

import com.payrail.checkout.dto.CheckoutViewResponse;
import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import com.payrail.payment.Payment;
import com.payrail.payment.PaymentRepository;
import com.payrail.payment.PaymentService;
import com.payrail.payment.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CheckoutService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public CheckoutService(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    public CheckoutViewResponse getCheckoutView(String paymentRef) {
        Payment payment = findPayableOrThrow(paymentRef, false);

        return new CheckoutViewResponse(
                payment.getPaymentRef(),
                payment.getMerchant().getBusinessName(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                payment.getReference(),
                payment.getStatus().name(),
                List.of("BANK", "QR")
        );
    }

    @Transactional
    public Payment pay(String paymentRef, String method) {
        Payment payment = findPayableOrThrow(paymentRef, true);
        return paymentService.initiatePayment(payment, method);
    }

    private Payment findPayableOrThrow(String paymentRef, boolean enforcePayable) {
        Payment payment = paymentRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Payment not found."));

        if (payment.getExpiresAt().isBefore(Instant.now()) && payment.getStatus() == PaymentStatus.PENDING) {
            throw new ApiException(ErrorCode.PAYMENT_EXPIRED, HttpStatus.GONE, "This payment has expired.");
        }

        if (enforcePayable && payment.getStatus() != PaymentStatus.PENDING) {
            throw new ApiException(ErrorCode.PAYMENT_NOT_PAYABLE, HttpStatus.CONFLICT,
                    "Payment is not in a payable state.");
        }

        return payment;
    }
}