package com.architek.oikos.user.domain.service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Pure domain service generating opaque, URL-safe verification tokens. Stateless,
 * no framework dependency (rule: domain depends on the JDK only).
 */
public final class VerificationTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
