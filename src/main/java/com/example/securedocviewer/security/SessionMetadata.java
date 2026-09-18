package com.example.securedocviewer.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.web.session.HttpSessionIdChangedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Where and when each session signed in, for the admin sessions list. In memory, like the sessions. */
@Component
public class SessionMetadata {

    public record Info(String clientIp, String device, Instant startedAt) {
    }

    private final Map<String, Info> bySessionId = new ConcurrentHashMap<>();

    public void recordSignIn(String sessionId, String clientIp, String userAgent) {
        bySessionId.put(sessionId, new Info(clientIp, describe(userAgent), Instant.now()));
    }

    public Info get(String sessionId) {
        return bySessionId.get(sessionId);
    }

    @EventListener
    public void onDestroyed(SessionDestroyedEvent event) {
        bySessionId.remove(event.getId());
    }

    @EventListener
    public void onIdChanged(HttpSessionIdChangedEvent event) {
        Info info = bySessionId.remove(event.getOldSessionId());
        if (info != null) {
            bySessionId.put(event.getNewSessionId(), info);
        }
    }

    /** "Chrome on Windows" style summary; the raw User-Agent is not kept. */
    static String describe(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }
        String ua = userAgent.toLowerCase();
        String browser = ua.contains("edg/") ? "Edge" : ua.contains("firefox/") ? "Firefox"
                : ua.contains("chrome/") || ua.contains("chromium") ? "Chrome" : ua.contains("safari/") ? "Safari"
                : ua.contains("curl/") ? "curl" : ua.contains("playwright") || ua.contains("headless") ? "Automated browser"
                : "Other client";
        String os = ua.contains("windows") ? "Windows" : ua.contains("android") ? "Android"
                : ua.contains("iphone") || ua.contains("ipad") ? "iOS" : ua.contains("mac os") ? "macOS"
                : ua.contains("linux") ? "Linux" : null;
        return os == null ? browser : browser + " on " + os;
    }
}
