package com.architek.oikos.auth.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Pure domain service generating opaque, URL-safe password-reset tokens and computing
 * their SHA-256 hash for storage. Stateless, no framework dependency (rule: domain
 * depends on the JDK only).
 *
 * <p>Only the hash is ever persisted, as for the refresh token ({@link
 * RefreshTokenSecretGenerator}): a database that leaks - a stray backup, an
 * operator's console - must not be enough to take over every account with a reset
 * request in flight.
 *
 * <p>Plain SHA-256 rather than BCrypt: the 32 bytes drawn here are already
 * unguessable, there is no dictionary to slow down - unlike a password a human
 * chose.
 */
public final class PasswordResetTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
