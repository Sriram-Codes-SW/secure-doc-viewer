package com.example.securedocviewer.security;

import com.example.securedocviewer.exception.LoginLockedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginThrottleTest {

    @Test
    void locksAnAccountFromOneAddressAfterRepeatedFailures() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT; i++) {
            throttle.checkAllowed("alice", "198.51.100.1");
            throttle.recordFailure("alice", "198.51.100.1");
        }
        LoginLockedException locked = assertThrows(LoginLockedException.class,
                () -> throttle.checkAllowed("alice", "198.51.100.1"));
        assertTrue(locked.getRetryAfterSeconds() > 0);
        // The same account from another address is not affected by that one address's lock.
        assertDoesNotThrow(() -> throttle.checkAllowed("alice", "198.51.100.2"));
    }

    @Test
    void rotatingSourceAddressesStillHitsTheAccountWideCap() {
        // Regression for spoofed/rotated X-Forwarded-For: every guess from a new
        // "address" used to start a fresh per-(account, IP) counter.
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT_ANY_IP; i++) {
            String ip = "203.0.113." + i;
            throttle.checkAllowed("bob", ip);
            throttle.recordFailure("bob", ip);
        }
        assertThrows(LoginLockedException.class, () -> throttle.checkAllowed("bob", "203.0.113.250"));
        assertDoesNotThrow(() -> throttle.checkAllowed("carol", "203.0.113.250"));
    }

    @Test
    void aSuccessClearsTheCounterForThatAddress() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT - 1; i++) {
            throttle.recordFailure("dave", "198.51.100.9");
        }
        throttle.recordSuccess("dave", "198.51.100.9");
        throttle.recordFailure("dave", "198.51.100.9");
        assertDoesNotThrow(() -> throttle.checkAllowed("dave", "198.51.100.9"));
    }
}
