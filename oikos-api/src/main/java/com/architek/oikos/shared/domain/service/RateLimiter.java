package com.architek.oikos.shared.domain.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window counter, keyed by whatever the caller decides identifies an abuser:
 * a client IP for the public endpoints, a target email address for the ones that
 * send mail. Pure JDK, no framework - the same instance serves the servlet filter
 * and the application services.
 *
 * <p>A fixed window rather than a sliding one, deliberately: the worst case is a
 * caller getting twice the quota by straddling two windows, and that costs a few
 * extra emails - where a sliding window costs a timestamp list per key, kept in
 * memory, for a public endpoint anyone can hammer.
 *
 * <p>Being refused does not extend the window. The point is to slow an abuser
 * down, not to lock out a legitimate user who kept clicking while blocked.
 *
 * <p>State lives in this process, so quotas are per instance: run two API
 * containers behind the proxy and each allows the full quota. That is a known and
 * accepted trade for now (the deployment runs a single container - see
 * docker-compose.yml); a shared counter would mean Redis, and a second moving
 * part to operate.
 */
public final class RateLimiter {

    /**
     * Beyond this many distinct keys, expired entries are swept before inserting a
     * new one. A flood of unique keys - one per forged IP - would otherwise grow
     * the map without bound, turning an anti-abuse device into the abuse.
     */
    private static final int SWEEP_THRESHOLD = 10_000;

    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    /**
     * Records one attempt for {@code key} and says whether it is allowed.
     * {@code retryAfter} is how long the caller must wait, zero when allowed.
     */
    public Decision tryConsume(String key, int limit, Duration window) {
        Instant now = clock.instant();
        if (windows.size() >= SWEEP_THRESHOLD) {
            windows.values().removeIf(existing -> existing.hasEnded(now));
        }
        Window current = windows.compute(key, (ignored, existing) ->
                existing == null || existing.hasEnded(now)
                        ? new Window(now.plus(window), 1)
                        : new Window(existing.endsAt(), existing.count() + 1));
        if (current.count() <= limit) {
            return new Decision(true, Duration.ZERO);
        }
        Duration retryAfter = Duration.between(now, current.endsAt());
        return new Decision(false, retryAfter.isNegative() ? Duration.ZERO : retryAfter);
    }

    /** Test seam: the number of keys currently held. */
    public int trackedKeys() {
        return windows.size();
    }

    public record Decision(boolean allowed, Duration retryAfter) {

        public boolean rejected() {
            return !allowed;
        }
    }

    private record Window(Instant endsAt, int count) {

        boolean hasEnded(Instant now) {
            return !now.isBefore(endsAt);
        }
    }
}
