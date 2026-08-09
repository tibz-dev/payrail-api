package com.payrail.auth.dto;

import java.time.Instant;

public record RegisterResponse(String merchantId, String email, Instant createdAt) {}