package com.architek.oikos.meeting.domain.service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates the opaque token behind a convocation's confirmation link.
 *
 * <p>32 bytes of SecureRandom: the token is the only thing an anonymous
 * visitor presents, so it is the only thing standing between a stranger and
 * answering on a lot's behalf. Guessing one has to be out of reach, and
 * anything derived from the convocation's own data - its id, the lot, the
 * meeting - would not be.
 *
 * <p>Same algorithm as InvitationTokenGenerator, kept as this module's own
 * copy rather than a cross-module dependency: the codebase already has several
 * near-identical generators living side by side, and that is the lesser evil
 * against `meeting` depending on `invitation`.
 */
public final class ConvocationTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
