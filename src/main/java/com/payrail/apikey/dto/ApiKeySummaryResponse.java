package com.payrail.apikey.dto;

import java.time.Instant;

public record ApiKeySummaryResponse(String id, String prefix, String lastFour, String status, Instant createdAt) {}