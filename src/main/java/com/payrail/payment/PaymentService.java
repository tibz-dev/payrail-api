package com.payrail.payment;

import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import com.payrail.common.id.PublicIdGenerator;
import com.payrail.merchant.Merchant;
import com.payrail.payment.dto.CreatePaymentRequest;
import com.payrail.payment.dto.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PaymentService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("ZAR");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHECKOUT_TOKEN_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final PaymentRepository paymentRepository;
    private final PublicIdGenerator idGenerator;

    public PaymentService(PaymentRepository paymentRepository, PublicIdGenerator idGenerator) {
        this.paymentRepository = paymentRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Payment createPayment(Merchant merchant, CreatePaymentRequest request) {
        if (!SUPPORTED_CURRENCIES.contains(request.currency())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Unsupported currency: " + request.currency());
        }

        String paymentRef = idGenerator.generate("pay_");
        String checkoutToken = generateCheckoutToken();
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);

        Payment payment = new Payment(paymentRef, merchant, request.amount(), request.currency(),
                request.reference(), request.description(), checkoutToken, expiresAt);

        return paymentRepository.save(payment);
    }

    public Payment findByRefOrThrow(String paymentRef) {
        return paymentRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Payment not found."));
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentRef(),
                payment.getStatus().name(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                payment.getReference(),
                "https://payrail.co.za/pay/" + payment.getPaymentRef(),
                payment.getExpiresAt(),
                payment.getCreatedAt()
        );
    }

    private String generateCheckoutToken() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(CHECKOUT_TOKEN_CHARS.charAt(RANDOM.nextInt(CHECKOUT_TOKEN_CHARS.length())));
        }
        return sb.toString();
    }
}