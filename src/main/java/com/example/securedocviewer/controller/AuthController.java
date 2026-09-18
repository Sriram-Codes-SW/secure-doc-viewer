package com.example.securedocviewer.controller;

import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import com.example.securedocviewer.audit.AuditEventType;
import com.example.securedocviewer.audit.AuditLogService;
import com.example.securedocviewer.audit.RequestActors;
import com.example.securedocviewer.exception.LoginLockedException;
import com.example.securedocviewer.security.KnownDevices;
import com.example.securedocviewer.security.LoginThrottle;
import com.example.securedocviewer.security.PasswordChangeRequiredFilter;
import com.example.securedocviewer.security.SessionMetadata;
import com.example.securedocviewer.security.SessionAdministration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Sign-in, sign-out and "who am I". The response never contains the session
 * id: it travels only in the httpOnly session cookie set by the container.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(@NotBlank @Size(max = 64) String username,
                               @NotBlank @Size(max = 128) String password) {
    }

    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {
    }

    /**
     * {@code sessionTimeoutSeconds} is the idle timeout, so the UI can warn
     * before it signs the user out; {@code mustChangePassword} sends the user
     * to the password form (the server refuses everything else meanwhile).
     */
    public record CurrentUser(String username, String role, int sessionTimeoutSeconds, boolean mustChangePassword) {
    }

    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final LoginThrottle loginThrottle;
    private final UserAccountService accounts;
    private final SessionAdministration sessions;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AuditLogService audit;
    private final RequestActors actors;
    private final Duration configuredSessionTimeout;
    private final KnownDevices knownDevices;
    private final SessionMetadata sessionMetadata;

    public AuthController(AuthenticationManager authenticationManager,
                          SessionAuthenticationStrategy sessionAuthenticationStrategy,
                          SecurityContextRepository securityContextRepository,
                          LoginThrottle loginThrottle,
                          UserAccountService accounts,
                          SessionAdministration sessions,
                          CsrfTokenRepository csrfTokenRepository,
                          AuditLogService audit,
                          RequestActors actors,
                          @Value("${server.servlet.session.timeout:30m}") Duration configuredSessionTimeout,
                          KnownDevices knownDevices,
                          SessionMetadata sessionMetadata) {
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
        this.loginThrottle = loginThrottle;
        this.accounts = accounts;
        this.sessions = sessions;
        this.csrfTokenRepository = csrfTokenRepository;
        this.audit = audit;
        this.actors = actors;
        this.configuredSessionTimeout = configuredSessionTimeout;
        this.knownDevices = knownDevices;
        this.sessionMetadata = sessionMetadata;
    }

    @PostMapping("/login")
    public ResponseEntity<CurrentUser> login(@Valid @RequestBody LoginRequest body,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        String username = UserAccountService.normalizeUsername(body.username());
        String clientIp = request.getRemoteAddr();
        Actor attempted = new Actor(username, null, clientIp);
        boolean recognisedDevice = knownDevices.isRecognised(username, clientIp);
        try {
            loginThrottle.checkAllowed(username, clientIp, recognisedDevice);
        } catch (LoginLockedException e) {
            // Once a minute per account+IP is enough to show a lockout without flooding the log.
            audit.recordAtMostEvery(Duration.ofMinutes(1), "locked:" + username + "|" + clientIp + "|" + e.getRule(),
                    AuditEventType.SIGN_IN_LOCKED, attempted, Subject.detail("rule=" + e.getRule()));
            throw e;
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, body.password()));
        } catch (AuthenticationException e) {
            // Same message for unknown user, wrong password and disabled
            // account, so the response doesn't reveal which accounts exist.
            loginThrottle.recordFailure(username, clientIp);
            audit.record(AuditEventType.SIGN_IN_FAILED, attempted, Subject.none());
            throw new BadCredentialsException("Invalid username or password.");
        }
        loginThrottle.recordSuccess(username, clientIp);
        knownDevices.remember(username, clientIp);
        boolean mustChangePassword = accounts.recordSignIn(username);

        // Ensure a session exists, then rotate its id and register it.
        request.getSession(true);
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        rotateCsrfToken(request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        HttpSession session = request.getSession();
        session.setAttribute(PasswordChangeRequiredFilter.SESSION_ATTRIBUTE, mustChangePassword);
        sessionMetadata.recordSignIn(session.getId(), clientIp, request.getHeader("User-Agent"));
        audit.record(AuditEventType.SIGN_IN, actors.of(request, username), Subject.none());

        return ResponseEntity.ok(toCurrentUser(authentication, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, Authentication authentication) {
        if (authentication != null) {
            audit.record(AuditEventType.SIGN_OUT, actors.of(request, authentication), Subject.none());
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public CurrentUser me(Authentication authentication, HttpServletRequest request) {
        return toCurrentUser(authentication, request);
    }

    /** Also ends the user's other sessions, so a password change locks out anyone who had it. */
    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest body,
                                               Authentication authentication,
                                               HttpServletRequest request) {
        accounts.changeOwnPassword(authentication.getName(), body.currentPassword(), body.newPassword());
        request.getSession().setAttribute(PasswordChangeRequiredFilter.SESSION_ATTRIBUTE, false);
        // The password just changed: this device is known-good again (knownDevices were cleared).
        knownDevices.remember(authentication.getName(), request.getRemoteAddr());
        sessions.revokeAllFor(authentication.getName(), request.getSession().getId());
        audit.record(AuditEventType.PASSWORD_CHANGED, actors.of(request, authentication), Subject.none());
        return ResponseEntity.noContent().build();
    }

    /**
     * Issues a brand-new CSRF token on the sign-in response. Spring's
     * CsrfAuthenticationStrategy deletes the cookie and then re-reads the
     * token from the request — which still carries the old cookie — so the
     * browser ends up with no token at all and its first write after signing
     * in fails with 403. Generating and saving explicitly sends exactly one
     * Set-Cookie with the new value.
     */
    private void rotateCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken fresh = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(fresh, request, response);
    }

    private CurrentUser toCurrentUser(Authentication authentication, HttpServletRequest request) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse("");
        // The container normally applies server.servlet.session.timeout to the session; fall back to
        // the configured value where it doesn't (e.g. mock sessions in tests).
        int sessionTimeout = request.getSession().getMaxInactiveInterval();
        if (sessionTimeout <= 0) {
            sessionTimeout = (int) configuredSessionTimeout.toSeconds();
        }
        Object flag = request.getSession().getAttribute(PasswordChangeRequiredFilter.SESSION_ATTRIBUTE);
        return new CurrentUser(authentication.getName(), role, sessionTimeout, Boolean.TRUE.equals(flag));
    }
}
