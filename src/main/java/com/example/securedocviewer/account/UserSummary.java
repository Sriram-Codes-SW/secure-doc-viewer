package com.example.securedocviewer.account;

/** What the admin UI sees about an account — never the password hash. */
public record UserSummary(String username, Role role, boolean enabled, long createdAtEpochSeconds) {

    static UserSummary of(AppUser user) {
        return new UserSummary(user.getUsername(), user.getRole(), user.isEnabled(),
                user.getCreatedAt().getEpochSecond());
    }
}
