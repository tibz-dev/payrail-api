package com.payrail.checkout.dto;

import jakarta.validation.constraints.NotBlank;

public record PayRequest(@NotBlank String method) {}