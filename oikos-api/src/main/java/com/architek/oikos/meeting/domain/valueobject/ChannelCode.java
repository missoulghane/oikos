package com.architek.oikos.meeting.domain.valueobject;

import java.util.Locale;
import java.util.Objects;

/**
 * The identity of a convocation channel, as a value.
 *
 * <p>Deliberately not an enum: the channels are a catalog row now, so that
 * adding one is an INSERT rather than a deployment. What the code may still
 * name are the two channels it can actually perform - {@link #EMAIL} and
 * {@link #APP} - because an emitter exists for them. Every other code is data
 * this class only carries around.
 */
public record ChannelCode(String value) {

    /** The two codes the application knows how to send by itself. */
    public static final ChannelCode EMAIL = new ChannelCode("EMAIL");
    public static final ChannelCode APP = new ChannelCode("APP");

    public ChannelCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("channel code must not be blank");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
    }

    public static ChannelCode of(String value) {
        return new ChannelCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
