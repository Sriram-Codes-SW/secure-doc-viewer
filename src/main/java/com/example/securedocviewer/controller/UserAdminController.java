package com.example.securedocviewer.controller;

import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.account.UserSummary;
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

    private final UserAccountService accounts;
    private final SessionAdministration sessions;

    public UserAdminController(UserAccountService accounts, SessionAdministration sessions) {
        this.accounts = accounts;
        this.sessions = sessions;
    }

    @GetMapping
    public List<UserSummary> list() {
        return accounts.list();
    }

    @PostMapping
    public ResponseEntity<UserSummary> create(@Valid @RequestBody CreateUserRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accounts.create(body.username(), body.password(), body.role()));
    }

    @PatchMapping("/{username}")
    public UserSummary update(@PathVariable String username,
                              @RequestBody UpdateUserRequest body,
                              Authentication authentication,
                              HttpServletRequest request) {
        UserSummary updated = accounts.update(authentication.getName(), username, body.role(), body.enabled());
        // Keep the caller's own session if they edited themselves.
        sessions.revokeAllFor(updated.username(), request.getSession().getId());
        return updated;
    }

    @PostMapping("/{username}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable String username,
                                              @Valid @RequestBody ResetPasswordRequest body,
                                              HttpServletRequest request) {
        accounts.resetPassword(username, body.password());
        sessions.revokeAllFor(UserAccountService.normalizeUsername(username), request.getSession().getId());
        return ResponseEntity.noContent().build();
    }
}
