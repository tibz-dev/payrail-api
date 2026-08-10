package com.payrail.payment;

import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import com.payrail.common.id.PublicIdGenerator;
import com.payrail.common.id.RequestHasher;
import com.payrail.merchant.Merchant;
import com.payrail.payment.dto.CreatePaymentRequest;
import com.payrail.payment.dto.PaymentResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class PaymentService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("ZAR");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHECKOUT_TOKEN_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final PublicIdGenerator idGenerator;
    private final RequestHasher requestHasher;

    public PaymentService(PaymentRepository paymentRepository,
                          IdempotencyKeyRepository idempotencyKeyRepository,
                          PublicIdGenerator idGenerator,
                          RequestHasher requestHasher) {
        this.paymentRepository = paymentRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.idGenerator = idGenerator;
        this.requestHasher = requestHasher;
    }

    @Transactional
    public PaymentCreationResult createPayment(Merchant merchant, CreatePaymentRequest request, String idempotencyKey) {
        if (!SUPPORTED_CURRENCIES.contains(request.currency())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Unsupported currency: " + request.currency());
        }

        String requestHash = requestHasher.hash(request.toString());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotencyKeyRepository.findByMerchantIdAndIdempotencyKey(merchant.getId(), idempotencyKey);

            if (existing.isPresent()) {
                if (!existing.get().getRequestHash().equals(requestHash)) {
                    throw new ApiException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT, HttpStatus.CONFLICT,
                            "This idempotency key was already used with a different request body.");
                }
                // Same key, same body — replay the original payment, not a new one.
                return new PaymentCreationResult(existing.get().getPayment(), true);
            }
        }

        Payment payment = buildAndSavePayment(merchant, request);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                idempotencyKeyRepository.save(new IdempotencyKeyEntity(merchant, idempotencyKey, requestHash, payment));
            } catch (DataIntegrityViolationException e) {
                // Lost a race: another request with the same key committed first.
                // Discard our payment attempt's key row (payment itself is harmless — just unreferenced)
                // and defer to whichever row won.
                var winner = idempotencyKeyRepository
                        .findByMerchantIdAndIdempotencyKey(merchant.getId(), idempotencyKey)
                        .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
                                "Idempotency conflict could not be resolved."));

                if (!winner.getRequestHash().equals(requestHash)) {
                    throw new ApiException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT, HttpStatus.CONFLICT,
                            "This idempotency key was already used with a different request body.");
                }
                return new PaymentCreationResult(winner.getPayment(), true);
            }
        }

        return new PaymentCreationResult(payment, false);
    }

    private Payment buildAndSavePayment(Merchant merchant, CreatePaymentRequest request) {
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