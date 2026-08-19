package com.architek.oikos.shared.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final Instant START = Instant.parse("2026-08-19T10:00:00Z");

    /** Horloge qu'on avance à la main : rien ici ne doit dépendre du temps réel. */
    private static final class MovableClock extends Clock {

        private Instant now = START;

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }
    }

    @Test
    void allows_up_to_the_limit_then_refuses() {
        RateLimiter limiter = new RateLimiter(new MovableClock());

        assertThat(limiter.tryConsume("ip", 3, WINDOW).allowed()).isTrue();
        assertThat(limiter.tryConsume("ip", 3, WINDOW).allowed()).isTrue();
        assertThat(limiter.tryConsume("ip", 3, WINDOW).allowed()).isTrue();
        assertThat(limiter.tryConsume("ip", 3, WINDOW).rejected()).isTrue();
    }

    @Test
    void says_how_long_to_wait() {
        MovableClock clock = new MovableClock();
        RateLimiter limiter = new RateLimiter(clock);
        limiter.tryConsume("ip", 1, WINDOW);

        clock.advance(Duration.ofMinutes(2));

        assertThat(limiter.tryConsume("ip", 1, WINDOW).retryAfter()).isEqualTo(Duration.ofMinutes(3));
    }

    @Test
    void a_refusal_does_not_push_the_window_further_out() {
        // Le but est de ralentir un abuseur, pas d'enfermer dehors l'utilisateur
        // légitime qui a continué de cliquer pendant qu'il était bloqué.
        MovableClock clock = new MovableClock();
        RateLimiter limiter = new RateLimiter(clock);
        limiter.tryConsume("ip", 1, WINDOW);
        clock.advance(Duration.ofMinutes(4));
        limiter.tryConsume("ip", 1, WINDOW);
        limiter.tryConsume("ip", 1, WINDOW);

        clock.advance(Duration.ofMinutes(1));

        assertThat(limiter.tryConsume("ip", 1, WINDOW).allowed()).isTrue();
    }

    @Test
    void the_window_reopens_once_it_has_passed() {
        MovableClock clock = new MovableClock();
        RateLimiter limiter = new RateLimiter(clock);
        limiter.tryConsume("ip", 1, WINDOW);
        assertThat(limiter.tryConsume("ip", 1, WINDOW).rejected()).isTrue();

        clock.advance(WINDOW);

        assertThat(limiter.tryConsume("ip", 1, WINDOW).allowed()).isTrue();
    }

    @Test
    void each_key_has_its_own_quota() {
        RateLimiter limiter = new RateLimiter(new MovableClock());
        limiter.tryConsume("first", 1, WINDOW);

        assertThat(limiter.tryConsume("first", 1, WINDOW).rejected()).isTrue();
        assertThat(limiter.tryConsume("second", 1, WINDOW).allowed()).isTrue();
    }

    @Test
    void expired_keys_are_swept_instead_of_growing_without_bound() {
        // Un endpoint public accepte autant de clés distinctes qu'un attaquant en
        // forge : sans balayage, l'anti-abus deviendrait lui-même l'abus.
        MovableClock clock = new MovableClock();
        RateLimiter limiter = new RateLimiter(clock);
        for (int i = 0; i < 10_000; i++) {
            limiter.tryConsume("key-" + i, 1, WINDOW);
        }
        assertThat(limiter.trackedKeys()).isEqualTo(10_000);

        clock.advance(WINDOW.plusSeconds(1));
        limiter.tryConsume("one-more", 1, WINDOW);

        assertThat(limiter.trackedKeys()).isEqualTo(1);
    }
}
