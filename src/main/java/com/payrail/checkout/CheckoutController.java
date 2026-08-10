package com.payrail.checkout;

import com.payrail.checkout.dto.CheckoutViewResponse;
import com.payrail.checkout.dto.PayRequest;
import com.payrail.payment.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final PaymentService paymentService;

    public CheckoutController(CheckoutService checkoutService, PaymentService paymentService) {
        this.checkoutService = checkoutService;
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<CheckoutViewResponse> view(@PathVariable String paymentId) {
        return ResponseEntity.ok(checkoutService.getCheckoutView(paymentId));
    }

    @PostMapping("/{paymentId}/pay")
    public ResponseEntity<Object> pay(@PathVariable String paymentId, @Valid @RequestBody PayRequest request) {
        var payment = checkoutService.pay(paymentId, request.method());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(paymentService.toResponse(payment));
    }
}