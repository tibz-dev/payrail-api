package com.payrail.common.id;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class PublicIdGenerator {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate(String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 20; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}