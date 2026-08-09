package com.payrail.apikey;

import com.payrail.apikey.dto.ApiKeyResponse;
import com.payrail.apikey.dto.ApiKeySummaryResponse;
import com.payrail.auth.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final JwtTokenProvider jwtTokenProvider;

    public ApiKeyController(ApiKeyService apiKeyService, JwtTokenProvider jwtTokenProvider) {
        this.apiKeyService = apiKeyService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponse> create(@RequestHeader("Authorization") String authHeader) {
        String merchantRef = jwtTokenProvider.getMerchantRef(extractToken(authHeader));
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createKey(merchantRef));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeySummaryResponse>> list(@RequestHeader("Authorization") String authHeader) {
        String merchantRef = jwtTokenProvider.getMerchantRef(extractToken(authHeader));
        return ResponseEntity.ok(apiKeyService.listKeys(merchantRef));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@RequestHeader("Authorization") String authHeader, @PathVariable UUID id) {
        String merchantRef = jwtTokenProvider.getMerchantRef(extractToken(authHeader));
        apiKeyService.revokeKey(merchantRef, id);
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String authHeader) {
        return authHeader.replaceFirst("(?i)^Bearer ", "").trim();
    }
}