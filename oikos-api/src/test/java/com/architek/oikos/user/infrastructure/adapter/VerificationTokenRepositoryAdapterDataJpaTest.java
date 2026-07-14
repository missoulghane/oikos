package com.architek.oikos.user.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.valueobject.UserId;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({VerificationTokenRepositoryAdapter.class, JpaAuditingConfiguration.class})
class VerificationTokenRepositoryAdapterDataJpaTest {

    @Autowired
    private VerificationTokenRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_token_by_its_value() {
        VerificationToken token = VerificationToken.issue(UserId.newId(), "opaque-token", Instant.now().plusSeconds(3600));

        adapter.save(token);

        assertThat(adapter.findByToken("opaque-token")).isPresent()
                .get().extracting(VerificationToken::userId).isEqualTo(token.userId());
    }

    @Test
    void deleteByUserId_removes_all_tokens_for_that_user() {
        UserId userId = UserId.newId();
        adapter.save(VerificationToken.issue(userId, "token-1", Instant.now().plusSeconds(3600)));

        adapter.deleteByUserId(userId);

        assertThat(adapter.findByToken("token-1")).isEmpty();
    }

    @Test
    void unknown_token_returns_empty() {
        assertThat(adapter.findByToken("does-not-exist")).isEmpty();
    }
}
