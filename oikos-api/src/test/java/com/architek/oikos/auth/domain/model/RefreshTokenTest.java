package com.architek.oikos.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.architek.oikos.shared.domain.valueobject.EntityId;

class RefreshTokenTest {

    @Test
    void revoke_returns_new_instance_with_same_id_but_revoked_true() {
        RefreshToken token = RefreshToken.issue(EntityId.newId(), "hash", Set.of("ROLE_USER"), Instant.now().plusSeconds(60));

        RefreshToken revoked = token.revoke();

        assertThat(revoked.id()).isEqualTo(token.id());
        assertThat(revoked.revoked()).isTrue();
        assertThat(token.revoked()).isFalse();
    }

    @Test
    void isUsable_is_false_when_expired() {
        RefreshToken token = RefreshToken.issue(EntityId.newId(), "hash", Set.of(), Instant.now().minusSeconds(1));

        assertThat(token.isUsable(Instant.now())).isFalse();
    }

    @Test
    void isUsable_is_false_when_revoked() {
        RefreshToken token = RefreshToken.issue(EntityId.newId(), "hash", Set.of(), Instant.now().plusSeconds(60)).revoke();

        assertThat(token.isUsable(Instant.now())).isFalse();
    }

    @Test
    void isUsable_is_true_when_neither_expired_nor_revoked() {
        RefreshToken token = RefreshToken.issue(EntityId.newId(), "hash", Set.of(), Instant.now().plusSeconds(60));

        assertThat(token.isUsable(Instant.now())).isTrue();
    }
}
