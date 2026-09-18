package com.example.securedocviewer.document;

import org.springframework.security.core.Authentication;

/** The signed-in user a document operation is performed for. */
public record Viewer(String username, boolean admin, boolean publisher) {

    public static Viewer of(Authentication authentication) {
        boolean admin = hasRole(authentication, "ROLE_ADMIN");
        return new Viewer(authentication.getName(), admin, admin || hasRole(authentication, "ROLE_PUBLISHER"));
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
