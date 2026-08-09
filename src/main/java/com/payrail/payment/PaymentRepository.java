package com.payrail.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPaymentRef(String paymentRef);
    Optional<Payment> findByCheckoutToken(String checkoutToken);
}