package com.example.securedocviewer.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.account.UserSummary;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import com.example.securedocviewer.audit.AuditEventType;
import com.example.securedocviewer.audit.AuditLogService;
import com.example.securedocviewer.audit.RequestActors;
import com.example.securedocviewer.document.DocumentRepository;
import com.example.securedocviewer.security.LoginThrottle;
import com.example.securedocviewer.security.SessionAdministration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Account management (ADMIN only). There is no self-registration: an admin
 * creates every account and hands out its initial password. Any change that
 * affects what a user may do also ends their current sessions, so it takes
 * effect immediately.
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    public record CreateUserRequest(@NotBlank String username, @NotBlank String password, @NotNull Role role) {
    }

    public record UpdateUserRequest(Role role, Boolean enabled) {
    }

    public record ResetPasswordRequest(@NotBlank String password) {
    }

    /** The admin list also shows how many documents each user owns (e.g. before disabling them). */
    public record AdminUserView(String username, com.example.securedocviewer.account.Role role, boolean enabled,
                                long createdAtEpochSeconds, Long lastSignInEpochSeconds, boolean mustChangePassword,
                                long ownedDocuments, boolean locked) {
    }

    private final UserAccountService accounts;
    private final SessionAdministration sessions;
    private final AuditLogService audit;
    private final RequestActors actors;
    private final LoginThrottle loginThrottle;
    private final DocumentRepository documents;

    public UserAdminController(UserAccountService accounts, SessionAdministration sessions,
                               AuditLogService audit, RequestActors actors, LoginThrottle loginThrottle,
                               DocumentRepository documents) {
        this.accounts = accounts;
        this.sessions = sessions;
        this.audit = audit;
        this.actors = actors;
        this.loginThrottle = loginThrottle;
        this.documents = documents;
    }

    @GetMapping
    public List<AdminUserView> list() {
        return accounts.list().stream()
                .map(u -> new AdminUserView(u.username(), u.role(), u.enabled(), u.createdAtEpochSeconds(),
                        u.lastSignInEpochSeconds(), u.mustChangePassword(),
                        documents.countByOwner_Username(u.username()), loginThrottle.isLocked(u.username())))
                .toList();
    }

    /** Clears the account's sign-in lockout counters (not other accounts' or per-address ones). */
    /** Ends every session of the user (except the admin's own, if they target themselves). */
    @DeleteMapping("/{username}/sessions")
    public ResponseEntity<Void> signOutEverywhere(@PathVariable String username, Authentication authentication,
                                                  HttpServletRequest request) {
        String target = UserAccountService.normalizeUsername(username);
        accounts.requireExists(target);
        sessions.revokeAllFor(target, request.getSession().getId());
        audit.record(AuditEventType.SESSION_REVOKED, actors.of(request, authentication),
                Subject.detail("all sessions of " + target));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{username}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable String username, Authentication authentication,
                                       HttpServletRequest request) {
        String normalized = UserAccountService.normalizeUsername(username);
        accounts.requireExists(normalized);
        loginThrottle.unlock(normalized);
        audit.record(AuditEventType.USER_UNLOCKED, actors.of(request, authentication), Subject.detail(normalized));
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UserSummary> create(@Valid @RequestBody CreateUserRequest body,
                                              Authentication authentication, HttpServletRequest request) {
        UserSummary created = accounts.create(body.username(), body.password(), body.role());
        audit.record(AuditEventType.USER_CREATED, actors.of(request, authentication),
                Subject.detail(created.username() + " as " + created.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{username}")
    public UserSummary update(@PathVariable String username,
                              @RequestBody UpdateUserRequest body,
                              Authentication authentication,
                              HttpServletRequest request) {
        UserSummary updated = accounts.update(authentication.getName(), username, body.role(), body.enabled());
        // Keep the caller's own session if they edited themselves.
        sessions.revokeAllFor(updated.username(), request.getSession().getId());
        audit.record(AuditEventType.USER_UPDATED, actors.of(request, authentication),
                Subject.detail(updated.username() + ": role=" + updated.role() + ", enabled=" + updated.enabled()));
        return updated;
    }

    @PostMapping("/{username}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable String username,
                                              @Valid @RequestBody ResetPasswordRequest body,
                                              Authentication authentication,
                                              HttpServletRequest request) {
        accounts.resetPassword(username, body.password());
        String normalized = UserAccountService.normalizeUsername(username);
        sessions.revokeAllFor(normalized, request.getSession().getId());
        audit.record(AuditEventType.USER_PASSWORD_RESET, actors.of(request, authentication), Subject.detail(normalized));
        return ResponseEntity.noContent().build();
    }
}
