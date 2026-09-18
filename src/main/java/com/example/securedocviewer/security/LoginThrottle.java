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
 * Slows password guessing with three rolling-window rules (15 minutes):
 * <ol>
 *   <li><b>account+ip</b> — 5 failures for one account from one address
 *       locks that address out of that account. Always applies, including to
 *       recognised devices, so someone sharing the victim's NAT can't guess
 *       freely.</li>
 *   <li><b>ip</b> — 20 failures from one address across any usernames locks
 *       that address (password spraying).</li>
 *   <li><b>account-wide</b> — 20 failures for one account across all
 *       addresses locks the account for <i>unrecognised</i> addresses, so
 *       rotating addresses can't buy unlimited guesses. Addresses the account
 *       recently signed in from ({@link KnownDevices}) are exempt, so an
 *       attacker can't lock the owner out of their usual device.</li>
 * </ol>
 * Attempts refused by a lock aren't counted as failures. Counters are
 * in-memory per instance (a restart clears them); recognised devices are in
 * the database. An admin can clear an account's counters ({@link #unlock}).
 */
@Component
public class LoginThrottle {

    /** Which rule refused a sign-in; recorded in the audit log. */
    public enum Rule {
        ACCOUNT_AND_IP("account+ip"), IP("ip"), ACCOUNT_WIDE("account-wide");

        private final String label;

        Rule(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    static final int MAX_FAILURES_PER_ACCOUNT = 5;
    static final int MAX_FAILURES_PER_IP = 20;
    static final int MAX_FAILURES_PER_ACCOUNT_ANY_IP = 20;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginThrottle() {
        this(Clock.systemUTC());
    }

    LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    /** @param recognisedDevice whether the account has recently signed in successfully from this address */
    public void checkAllowed(String username, String clientIp, boolean recognisedDevice) {
        Instant now = clock.instant();
        check(Rule.ACCOUNT_AND_IP, lockedFor(accountKey(username, clientIp), MAX_FAILURES_PER_ACCOUNT, now));
        check(Rule.IP, lockedFor(ipKey(clientIp), MAX_FAILURES_PER_IP, now));
        if (!recognisedDevice) {
            check(Rule.ACCOUNT_WIDE, lockedFor(anyIpKey(username), MAX_FAILURES_PER_ACCOUNT_ANY_IP, now));
        }
    }

    public void recordFailure(String username, String clientIp) {
        Instant now = clock.instant();
        append(accountKey(username, clientIp), now);
        append(ipKey(clientIp), now);
        append(anyIpKey(username), now);
    }

    public void recordSuccess(String username, String clientIp) {
        failures.remove(accountKey(username, clientIp));
    }

    /**
     * Clears the account's counters (account+ip for every address, and
     * account-wide). Per-address counters are left alone: they belong to the
     * address, not the account.
     */
    public void unlock(String username) {
        String accountPrefix = "account:" + username + "|";
        failures.keySet().removeIf(key -> key.startsWith(accountPrefix) || key.equals(anyIpKey(username)));
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

    private static void check(Rule rule, long retryAfterSeconds) {
        if (retryAfterSeconds > 0) {
            throw new LoginLockedException(retryAfterSeconds, rule.label());
        }
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

    private static String anyIpKey(String username) {
        return "user:" + username;
    }

    private static String ipKey(String clientIp) {
        return "ip:" + clientIp;
    }
}
