package com.architek.oikos.messaging.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ConversationSubjectTest {

    @Test
    void accepts_a_non_blank_subject_within_the_length_limit() {
        ConversationSubject subject = ConversationSubject.of("Fuite d'eau hall B");

        assertThat(subject.value()).isEqualTo("Fuite d'eau hall B");
    }

    @Test
    void rejects_a_blank_subject() {
        assertThatThrownBy(() -> ConversationSubject.of("   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_a_null_subject() {
        assertThatThrownBy(() -> ConversationSubject.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejects_a_subject_longer_than_200_characters() {
        String tooLong = "a".repeat(201);

        assertThatThrownBy(() -> ConversationSubject.of(tooLong)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepts_a_subject_exactly_at_the_200_character_limit() {
        String maxLength = "a".repeat(200);

        assertThat(ConversationSubject.of(maxLength).value()).hasSize(200);
    }
}
