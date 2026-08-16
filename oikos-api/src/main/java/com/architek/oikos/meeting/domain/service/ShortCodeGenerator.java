package com.architek.oikos.meeting.domain.service;

import java.security.SecureRandom;

/**
 * Six characters a person can copy off a sheet of paper: the convocation's
 * confirmation code, and the public reference of the meeting it belongs to.
 *
 * <p>The alphabet is digits and lowercase letters, minus {@code l} and
 * {@code o}. Those two are the ones misread as {@code 1} and {@code 0} on
 * print, and this code exists precisely to be read off print - keeping them
 * would trade a rendering nicety for a copropriétaire who cannot confirm.
 * The digits stay: once their look-alikes are gone they are unambiguous.
 *
 * <p>SecureRandom rather than Random despite the small space. It costs nothing
 * here, and a predictable sequence would make the codes of a whole meeting
 * derivable from one of them - which is exactly the property the per-meeting
 * uniqueness is meant to deny.
 *
 * <p>34^6 is about 1.5 billion, six orders of magnitude below the 32-byte
 * token. That is why this code is never the only thing standing in front of a
 * convocation: it is presented with its meeting's reference, and the attempts
 * are capped (ADR 0002 §13).
 */
public final class ShortCodeGenerator {

    /** No l, no o - see the class comment. */
    private static final char[] ALPHABET = "0123456789abcdefghijkmnpqrstuvwxyz".toCharArray();
    private static final int LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET[SECURE_RANDOM.nextInt(ALPHABET.length)]);
        }
        return code.toString();
    }
}
