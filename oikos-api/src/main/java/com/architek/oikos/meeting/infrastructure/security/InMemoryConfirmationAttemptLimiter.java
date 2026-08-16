package com.architek.oikos.meeting.infrastructure.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.ConfirmationAttemptLimiterPort;

/**
 * Caps how often a wrong confirmation code may be tried from one place, per
 * meeting.
 *
 * <p>Counted per (meeting, caller) rather than per code: the point is to stop
 * someone walking the code space of an assembly, and they would use a different
 * code every time. Only failures count - a copropriétaire confirming, changing
 * their mind and confirming again is not an attacker.
 *
 * <p><strong>What this is not.</strong> It is an in-memory counter: it holds
 * within one instance and it forgets everything on restart. Behind two replicas
 * it allows twice the attempts, and a rolling deploy resets it. That is a real
 * limitation, stated here rather than discovered later - it raises the cost of
 * walking 34^6 by orders of magnitude, which is what makes the short code
 * defensible, and it is not a distributed rate limiter. Moving it to one (Redis,
 * or a table) is the change to make when the product runs on more than one
 * instance.
 *
 * <p>The caller is identified by IP, which a determined attacker rotates. Same
 * answer: this raises cost, and the strong secret is still the token.
 */
@Component
public class InMemoryConfirmationAttemptLimiter implements ConfirmationAttemptLimiterPort {

    private static final int MAX_FAILURES = 10;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int SWEEP_THRESHOLD = 10_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryConfirmationAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    /** True while this caller may still try a code for this meeting. */
    @Override
    public boolean isAllowed(String meetingReference, String callerId) {
        Window window = windows.get(key(meetingReference, callerId));
        return window == null || window.hasExpired(clock.instant()) || window.failures.get() < MAX_FAILURES;
    }

    @Override
    public void recordFailure(String meetingReference, String callerId) {
        Instant now = clock.instant();
        windows.compute(key(meetingReference, callerId), (ignored, existing) -> {
            if (existing == null || existing.hasExpired(now)) {
                return new Window(now);
            }
            existing.failures.incrementAndGet();
            return existing;
        });
        // Opportunistic sweep: without it the map only ever grows, and a public endpoint that
        // grows a map entry per caller is a memory leak with an attacker's name on it.
        if (windows.size() > SWEEP_THRESHOLD) {
            windows.values().removeIf(window -> window.hasExpired(now));
        }
    }

    /** A success clears the slate: the caller evidently holds a real code. */
    @Override
    public void recordSuccess(String meetingReference, String callerId) {
        windows.remove(key(meetingReference, callerId));
    }

    private static String key(String meetingReference, String callerId) {
        return meetingReference + '|' + callerId;
    }

    private static final class Window {

        private final Instant startedAt;
        private final AtomicInteger failures = new AtomicInteger(1);

        private Window(Instant startedAt) {
            this.startedAt = startedAt;
        }

        private boolean hasExpired(Instant now) {
            return startedAt.plus(WINDOW).isBefore(now);
        }
    }
}
