package com.architek.oikos.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RefreshTokenSecretGeneratorTest {

    private final RefreshTokenSecretGenerator generator = new RefreshTokenSecretGenerator();

    @Test
    void generates_unique_tokens() {
        assertThat(generator.generateOpaqueToken()).isNotEqualTo(generator.generateOpaqueToken());
    }

    @Test
    void hash_is_deterministic() {
        String token = generator.generateOpaqueToken();

        assertThat(generator.hash(token)).isEqualTo(generator.hash(token));
    }

    @Test
    void hash_differs_for_different_tokens() {
        assertThat(generator.hash("token-a")).isNotEqualTo(generator.hash("token-b"));
    }
}
