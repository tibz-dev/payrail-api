package com.payrail.merchant;

import com.payrail.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "merchant_ref", nullable = false, unique = true)
    private String merchantRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Merchant(String merchantRef, User user, String businessName, String contactName) {
        this.merchantRef = merchantRef;
        this.user = user;
        this.businessName = businessName;
        this.contactName = contactName;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}