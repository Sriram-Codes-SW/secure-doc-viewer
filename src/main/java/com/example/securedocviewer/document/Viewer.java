package com.example.securedocviewer.document;

import org.springframework.security.core.Authentication;

/** The signed-in user a document operation is performed for. */
public record Viewer(String username, boolean admin) {

    public static Viewer of(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return new Viewer(authentication.getName(), admin);
    }
}
