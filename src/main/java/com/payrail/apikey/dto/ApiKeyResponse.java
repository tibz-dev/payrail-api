package com.payrail.apikey.dto;

import java.time.Instant;

public record ApiKeyResponse(String id, String apiKey, Instant createdAt) {}