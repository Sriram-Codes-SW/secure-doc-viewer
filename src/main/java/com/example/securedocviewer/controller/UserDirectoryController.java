package com.example.securedocviewer.controller;

import com.example.securedocviewer.account.AppUser;
import com.example.securedocviewer.account.AppUserRepository;
import com.example.securedocviewer.account.UserAccountService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Username lookup for the "share with" picker. Restricted to PUBLISHER and
 * ADMIN in SecurityConfig (readers never share), returns usernames only, and
 * at most 20 per query, so it can't be used to dump account details.
 */
@RestController
@RequestMapping("/api/users")
public class UserDirectoryController {

    private final AppUserRepository users;

    public UserDirectoryController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping
    public List<String> search(@RequestParam(defaultValue = "") String q, Authentication authentication) {
        String prefix = UserAccountService.normalizeUsername(q);
        // Nothing for one character, and never admins (they can already see every document),
        // so the picker can't be used to list the account directory.
        if (prefix.length() < 2 || prefix.length() > 32) {
            return List.of();
        }
        return users.findTop20ByEnabledTrueAndUsernameStartingWithOrderByUsernameAsc(prefix).stream()
                .filter(u -> u.getRole() != com.example.securedocviewer.account.Role.ADMIN)
                .map(AppUser::getUsername)
                .filter(username -> !username.equals(authentication.getName()))
                .toList();
    }
}
