package com.example.securedocviewer.security;

import com.example.securedocviewer.exception.LoginLockedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows password guessing. Failures are counted in a rolling window per
 * (username, client IP) — so one attacker can't lock a real user out from
 * everywhere — and separately per client IP, so spraying many usernames from
 * one address is capped too. A success clears that account's counter.
 *
 * <p>In-memory, so counts reset on restart and aren't shared across
 * instances; that's acceptable for a throttle (it only ever errs on the
 * side of letting a request through).
 */
@Component
public class LoginThrottle {

    static final int MAX_FAILURES_PER_ACCOUNT = 5;
    static final int MAX_FAILURES_PER_IP = 20;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginThrottle() {
        this(Clock.systemUTC());
    }

    LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    public void checkAllowed(String username, String clientIp) {
        Instant now = clock.instant();
        long retryAfter = Math.max(
                lockedFor(accountKey(username, clientIp), MAX_FAILURES_PER_ACCOUNT, now),
                lockedFor(ipKey(clientIp), MAX_FAILURES_PER_IP, now));
        if (retryAfter > 0) {
            throw new LoginLockedException(retryAfter);
        }
    }

    public void recordFailure(String username, String clientIp) {
        Instant now = clock.instant();
        append(accountKey(username, clientIp), now);
        append(ipKey(clientIp), now);
    }

    public void recordSuccess(String username, String clientIp) {
        failures.remove(accountKey(username, clientIp));
    }

    /** Drops counters whose failures have all aged out, so the map can't grow without bound. */
    @Scheduled(fixedDelay = 300_000)
    public void sweep() {
        Instant cutoff = clock.instant().minus(WINDOW);
        failures.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                prune(entry.getValue(), cutoff);
                return entry.getValue().isEmpty();
            }
        });
    }

    private long lockedFor(String key, int maxFailures, Instant now) {
        Deque<Instant> attempts = failures.get(key);
        if (attempts == null) {
            return 0;
        }
        synchronized (attempts) {
            prune(attempts, now.minus(WINDOW));
            if (attempts.size() < maxFailures) {
                return 0;
            }
            // Unlocks when enough of the oldest failures have aged out of the window.
            Instant unlocksAt = attempts.stream()
                    .skip(attempts.size() - maxFailures)
                    .findFirst()
                    .orElseThrow()
                    .plus(WINDOW);
            return Math.max(1, Duration.between(now, unlocksAt).toSeconds());
        }
    }

    private void append(String key, Instant at) {
        Deque<Instant> attempts = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (attempts) {
            attempts.addLast(at);
        }
    }

    private static void prune(Deque<Instant> attempts, Instant cutoff) {
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
            attempts.pollFirst();
        }
    }

    private static String accountKey(String username, String clientIp) {
        return "account:" + username + "|" + clientIp;
    }

    private static String ipKey(String clientIp) {
        return "ip:" + clientIp;
    }
}
