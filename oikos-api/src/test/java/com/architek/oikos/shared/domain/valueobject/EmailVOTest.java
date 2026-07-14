package com.architek.oikos.shared.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailVOTest {

    @Test
    void normalizes_email_to_lowercase() {
        EmailVO email = EmailVO.of("Someone@Example.COM");

        assertThat(email.value()).isEqualTo("someone@example.com");
    }

    @Test
    void two_emails_differing_only_by_case_are_equal() {
        assertThat(EmailVO.of("Someone@Example.com")).isEqualTo(EmailVO.of("someone@example.com"));
    }

    @Test
    void rejects_invalid_format() {
        assertThatThrownBy(() -> EmailVO.of("not-an-email")).isInstanceOf(IllegalArgumentException.class);
    }
}
