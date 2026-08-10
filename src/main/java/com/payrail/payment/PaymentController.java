package com.payrail.payment;

import com.payrail.merchant.MerchantRepository;
import com.payrail.payment.dto.CreatePaymentRequest;
import com.payrail.payment.dto.PaymentResponse;
import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantRepository merchantRepository;

    public PaymentController(PaymentService paymentService, MerchantRepository merchantRepository) {
        this.paymentService = paymentService;
        this.merchantRepository = merchantRepository;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {

        var merchant = resolveMerchant(authentication);
        var result = paymentService.createPayment(merchant, request, idempotencyKey);

        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;

        return ResponseEntity.status(status)
                .header("Idempotency-Replayed", String.valueOf(result.replayed()))
                .body(paymentService.toResponse(result.payment()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable String id) {
        Payment payment = paymentService.findByRefOrThrow(id);
        return ResponseEntity.ok(paymentService.toResponse(payment));
    }

    private com.payrail.merchant.Merchant resolveMerchant(Authentication authentication) {
        String merchantRef = (String) authentication.getPrincipal();
        return merchantRepository.findByMerchantRef(merchantRef)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Merchant not found."));
    }
}