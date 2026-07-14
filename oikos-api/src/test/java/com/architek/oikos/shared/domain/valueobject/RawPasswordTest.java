package com.architek.oikos.shared.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RawPasswordTest {

    @Test
    void rejects_passwords_shorter_than_minimum_length() {
        assertThatThrownBy(() -> RawPassword.of("short")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepts_passwords_at_minimum_length() {
        String value = "a".repeat(RawPassword.MIN_LENGTH);

        assertThat(RawPassword.of(value).value()).isEqualTo(value);
    }

    @Test
    void toString_never_leaks_the_raw_value() {
        RawPassword password = RawPassword.of("super-secret-password");

        assertThat(password.toString()).doesNotContain("super-secret-password");
    }
}
