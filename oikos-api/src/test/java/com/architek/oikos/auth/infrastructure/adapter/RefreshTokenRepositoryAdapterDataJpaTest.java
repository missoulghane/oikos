package com.architek.oikos.auth.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.auth.domain.model.RefreshToken;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RefreshTokenRepositoryAdapter.class, JpaAuditingConfiguration.class})
class RefreshTokenRepositoryAdapterDataJpaTest {

    @Autowired
    private RefreshTokenRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_refresh_token_by_its_hash() {
        RefreshToken token = RefreshToken.issue(EntityId.newId(), "some-hash", Set.of("ROLE_USER"), Instant.now().plusSeconds(60));

        adapter.save(token);

        assertThat(adapter.findByTokenHash("some-hash")).isPresent()
                .get().satisfies(found -> {
                    assertThat(found.userId()).isEqualTo(token.userId());
                    assertThat(found.revoked()).isFalse();
                });
    }

    @Test
    void updating_an_existing_token_overwrites_it_rather_than_duplicating() {
        RefreshToken token = RefreshToken.issue(EntityId.newId(), "another-hash", Set.of(), Instant.now().plusSeconds(60));
        adapter.save(token);

        adapter.save(token.revoke());

        assertThat(adapter.findByTokenHash("another-hash")).isPresent().get()
                .extracting(RefreshToken::revoked).isEqualTo(true);
    }

    @Test
    void unknown_hash_returns_empty() {
        assertThat(adapter.findByTokenHash("does-not-exist")).isEmpty();
    }
}
