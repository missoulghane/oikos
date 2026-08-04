package com.architek.oikos.invitation.domain.service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Pure domain service generating opaque, URL-safe invitation tokens. Same
 * algorithm as PartyInvitationTokenGenerator, kept as invitation's own copy
 * rather than a cross-module dependency (this codebase already has several
 * near-identical token generators living side by side, e.g.
 * PartyInvitationTokenGenerator/VerificationTokenGenerator in user).
 */
public final class InvitationTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
