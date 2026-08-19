package com.architek.oikos.shared.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.architek.oikos.shared.domain.service.RateLimiter;
import com.architek.oikos.shared.exception.TooManyRequestsException;

class RateLimitedEmailSendQuotaTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-08-19T10:00:00Z"), ZoneOffset.UTC);

    private static RateLimitedEmailSendQuota quotaOf(boolean enabled, int perAddress) {
        RateLimitPolicies policies = new RateLimitPolicies(enabled, Duration.ofMinutes(5), 30, 10, 20, 10, perAddress);
        return new RateLimitedEmailSendQuota(new RateLimiter(FIXED_CLOCK), policies);
    }

    @Test
    void lets_the_first_sends_through_then_refuses() {
        RateLimitedEmailSendQuota quota = quotaOf(true, 2);

        quota.requireQuota("jane@doe.com");
        quota.requireQuota("jane@doe.com");

        assertThatThrownBy(() -> quota.requireQuota("jane@doe.com"))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("patienter");
    }

    @Test
    void the_refusal_says_how_long_to_wait() {
        RateLimitedEmailSendQuota quota = quotaOf(true, 1);
        quota.requireQuota("jane@doe.com");

        assertThatThrownBy(() -> quota.requireQuota("jane@doe.com"))
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(TooManyRequestsException.class))
                .satisfies(ex -> assertThat(ex.retryAfter()).isEqualTo(Duration.ofMinutes(5)));
    }

    @Test
    void changing_the_case_of_the_address_does_not_open_a_second_quota() {
        // Une seule boîte reçoit les messages, quelle que soit la casse tapée :
        // sans normalisation, le plafond se multiplierait par le nombre de
        // variantes qu'un appelant a la patience d'essayer.
        RateLimitedEmailSendQuota quota = quotaOf(true, 1);
        quota.requireQuota("jane@doe.com");

        assertThatThrownBy(() -> quota.requireQuota("Jane@Doe.COM"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void two_different_addresses_keep_their_own_quota() {
        RateLimitedEmailSendQuota quota = quotaOf(true, 1);
        quota.requireQuota("jane@doe.com");

        assertThatCode(() -> quota.requireQuota("john@doe.com")).doesNotThrowAnyException();
    }

    @Test
    void nothing_is_counted_when_the_anti_abuse_is_switched_off() {
        RateLimitedEmailSendQuota quota = quotaOf(false, 1);

        assertThatCode(() -> {
            quota.requireQuota("jane@doe.com");
            quota.requireQuota("jane@doe.com");
            quota.requireQuota("jane@doe.com");
        }).doesNotThrowAnyException();
    }
}
