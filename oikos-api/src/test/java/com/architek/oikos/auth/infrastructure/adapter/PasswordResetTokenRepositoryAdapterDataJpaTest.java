package com.architek.oikos.auth.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PasswordResetTokenRepositoryAdapter.class, JpaAuditingConfiguration.class})
class PasswordResetTokenRepositoryAdapterDataJpaTest {

    @Autowired
    private PasswordResetTokenRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_token_by_its_hash() {
        PasswordResetToken token = PasswordResetToken.issue(EntityId.newId(), "token-hash", Instant.now().plusSeconds(3600));

        adapter.save(token);

        assertThat(adapter.findByTokenHash("token-hash")).isPresent()
                .get().extracting(PasswordResetToken::userId).isEqualTo(token.userId());
    }

    @Test
    void deleteByUserId_removes_all_tokens_for_that_user() {
        EntityId userId = EntityId.newId();
        adapter.save(PasswordResetToken.issue(userId, "hash-1", Instant.now().plusSeconds(3600)));

        adapter.deleteByUserId(userId);

        assertThat(adapter.findByTokenHash("hash-1")).isEmpty();
    }

    @Test
    void unknown_token_returns_empty() {
        assertThat(adapter.findByTokenHash("does-not-exist")).isEmpty();
    }
}
