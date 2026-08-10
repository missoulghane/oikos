package com.architek.oikos.messaging.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MessageBodyTest {

    @Test
    void accepts_a_non_blank_body_within_the_length_limit() {
        MessageBody body = MessageBody.of("Bonjour tout le monde");

        assertThat(body.value()).isEqualTo("Bonjour tout le monde");
    }

    @Test
    void rejects_a_blank_body() {
        assertThatThrownBy(() -> MessageBody.of("   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_a_null_body() {
        assertThatThrownBy(() -> MessageBody.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejects_a_body_longer_than_4000_characters() {
        String tooLong = "a".repeat(4001);

        assertThatThrownBy(() -> MessageBody.of(tooLong)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepts_a_body_exactly_at_the_4000_character_limit() {
        String maxLength = "a".repeat(4000);

        assertThat(MessageBody.of(maxLength).value()).hasSize(4000);
    }
}
