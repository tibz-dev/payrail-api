package com.payrail.apikey;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.payrail.apikey.dto.ApiKeyResponse;
import com.payrail.apikey.dto.ApiKeySummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant/api-keys")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponse> create(Authentication authentication) {
        String merchantRef = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createKey(merchantRef));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeySummaryResponse>> list(Authentication authentication) {
        String merchantRef = (String) authentication.getPrincipal();
        return ResponseEntity.ok(apiKeyService.listKeys(merchantRef));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(Authentication authentication, @PathVariable UUID id) {
        String merchantRef = (String) authentication.getPrincipal();
        apiKeyService.revokeKey(merchantRef, id);
        return ResponseEntity.noContent().build();
    }
}