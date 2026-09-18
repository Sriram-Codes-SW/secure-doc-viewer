package com.example.securedocviewer.security;

import com.example.securedocviewer.exception.LoginLockedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginThrottleTest {

    private static void fail(LoginThrottle throttle, String user, String ip, int times) {
        for (int i = 0; i < times; i++) {
            throttle.recordFailure(user, ip);
        }
    }

    @Test
    void locksOneAddressOutOfAnAccountAfterFiveFailures() {
        LoginThrottle throttle = new LoginThrottle();
        fail(throttle, "alice", "198.51.100.1", LoginThrottle.MAX_FAILURES_PER_ACCOUNT);

        LoginLockedException locked = assertThrows(LoginLockedException.class,
                () -> throttle.checkAllowed("alice", "198.51.100.1", false));
        assertEquals("account+ip", locked.getRule());
        assertTrue(locked.getRetryAfterSeconds() > 0);
        assertDoesNotThrow(() -> throttle.checkAllowed("alice", "198.51.100.2", false));
    }

    @Test
    void theAccountAndAddressLockAppliesEvenToARecognisedDevice() {
        // D3: someone sharing the owner's NAT/address must not get unlimited guesses.
        LoginThrottle throttle = new LoginThrottle();
        fail(throttle, "alice", "198.51.100.1", LoginThrottle.MAX_FAILURES_PER_ACCOUNT);

        LoginLockedException locked = assertThrows(LoginLockedException.class,
                () -> throttle.checkAllowed("alice", "198.51.100.1", true));
        assertEquals("account+ip", locked.getRule());
    }

    @Test
    void rotatingAddressesHitTheAccountWideCapButRecognisedDevicesAreExempt() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT_ANY_IP; i++) {
            throttle.recordFailure("bob", "203.0.113." + i);
        }

        LoginLockedException locked = assertThrows(LoginLockedException.class,
                () -> throttle.checkAllowed("bob", "203.0.113.250", false));
        assertEquals("account-wide", locked.getRule());
        // The owner's usual device is not locked out by someone else's guesses.
        assertDoesNotThrow(() -> throttle.checkAllowed("bob", "192.0.2.10", true));
        // Other accounts are unaffected.
        assertDoesNotThrow(() -> throttle.checkAllowed("carol", "203.0.113.250", false));
    }

    @Test
    void sprayingManyUsernamesFromOneAddressLocksThatAddress() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_IP; i++) {
            throttle.recordFailure("user" + i, "198.51.100.66");
        }
        LoginLockedException locked = assertThrows(LoginLockedException.class,
                () -> throttle.checkAllowed("someone-new", "198.51.100.66", true));
        assertEquals("ip", locked.getRule());
    }

    @Test
    void unlockClearsTheAccountsCountersButNotThePerAddressOnes() {
        LoginThrottle throttle = new LoginThrottle();
        fail(throttle, "dave", "198.51.100.7", LoginThrottle.MAX_FAILURES_PER_ACCOUNT);
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_ACCOUNT_ANY_IP; i++) {
            throttle.recordFailure("dave", "203.0.113." + i);
        }
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_IP; i++) {
            throttle.recordFailure("spray" + i, "198.51.100.99");
        }

        throttle.unlock("dave");

        assertDoesNotThrow(() -> throttle.checkAllowed("dave", "198.51.100.7", false));
        assertDoesNotThrow(() -> throttle.checkAllowed("dave", "203.0.113.200", false));
        // The spraying address stays locked: that counter belongs to the address.
        assertThrows(LoginLockedException.class, () -> throttle.checkAllowed("dave", "198.51.100.99", false));
    }

    @Test
    void aSuccessClearsTheCounterForThatAddress() {
        LoginThrottle throttle = new LoginThrottle();
        fail(throttle, "erin", "198.51.100.9", LoginThrottle.MAX_FAILURES_PER_ACCOUNT - 1);
        throttle.recordSuccess("erin", "198.51.100.9");
        throttle.recordFailure("erin", "198.51.100.9");
        assertDoesNotThrow(() -> throttle.checkAllowed("erin", "198.51.100.9", false));
    }

    @Test
    void parallelAttemptsCannotAllSlipPastTheCheck() throws Exception {
        LoginThrottle throttle = new LoginThrottle();
        int threads = 40;
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger admitted = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        try {
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    start.await();
                    try {
                        throttle.reserve("frank", "198.51.100.10", false);
                        admitted.incrementAndGet(); // the password then turns out wrong: the failure stands
                    } catch (LoginLockedException refused) {
                        // expected for all but the first five
                    }
                    return null;
                });
            }
            start.countDown();
        } finally {
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS));
        }
        assertEquals(LoginThrottle.MAX_FAILURES_PER_ACCOUNT, admitted.get());
    }

    @Test
    void aSuccessfulReservationIsHandedBack() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < LoginThrottle.MAX_FAILURES_PER_IP - 1; i++) {
            throttle.recordFailure("spray" + i, "198.51.100.11");
        }
        java.time.Instant attempt = throttle.reserve("gina", "198.51.100.11", false);
        throttle.succeeded("gina", "198.51.100.11", attempt);
        // The correct password must not have used up the address's last allowed failure.
        assertDoesNotThrow(() -> throttle.reserve("harry", "198.51.100.11", false));
    }
}
