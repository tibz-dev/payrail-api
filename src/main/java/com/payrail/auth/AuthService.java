package com.payrail.auth;

import com.payrail.auth.dto.*;
import com.payrail.common.error.ApiException;
import com.payrail.common.error.ErrorCode;
import com.payrail.common.id.PublicIdGenerator;
import com.payrail.merchant.Merchant;
import com.payrail.merchant.MerchantRepository;
import com.payrail.user.User;
import com.payrail.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublicIdGenerator idGenerator;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, MerchantRepository merchantRepository,
                       PasswordEncoder passwordEncoder, PublicIdGenerator idGenerator,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT,
                    "An account with this email already exists.");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String merchantRef = idGenerator.generate("mch_");
        Merchant merchant = new Merchant(merchantRef, user, request.businessName(), request.contactName());
        merchantRepository.save(merchant);

        return new RegisterResponse(merchantRef, user.getEmail(), merchant.getCreatedAt());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED,
                        "Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED,
                    "Invalid email or password.");
        }

        Merchant merchant = merchantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED,
                        "No merchant associated with this account."));

        String token = jwtTokenProvider.generateToken(merchant.getMerchantRef());
        return new AuthResponse(token, jwtTokenProvider.getExpiry(token));
    }
}