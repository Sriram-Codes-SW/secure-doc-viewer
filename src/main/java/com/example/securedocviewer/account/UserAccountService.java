package com.example.securedocviewer.account;

import com.example.securedocviewer.exception.BadRequestException;
import com.example.securedocviewer.exception.ResourceNotFoundException;
import com.example.securedocviewer.exception.UsernameTakenException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The only place accounts are created or changed. Usernames are normalised
 * to lower case so "Alice" and "alice" can never be two different people —
 * the username is what gets burned into every watermark, so it has to be
 * unambiguous.
 */
@Service
public class UserAccountService {

    private static final Pattern USERNAME = Pattern.compile("[a-z0-9._-]{3,32}");
    static final int MIN_PASSWORD_LENGTH = 12;
    static final int MAX_PASSWORD_LENGTH = 128;

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public UserSummary create(String rawUsername, String password, Role role) {
        String username = normalizeUsername(rawUsername);
        if (!USERNAME.matcher(username).matches()) {
            throw new BadRequestException(
                    "Username must be 3-32 characters: lower-case letters, digits, '.', '_' or '-'.");
        }
        requireAcceptablePassword(password);
        if (role == null) {
            throw new BadRequestException("A role is required.");
        }
        if (repository.existsByUsername(username)) {
            throw new UsernameTakenException(username);
        }
        return UserSummary.of(repository.save(new AppUser(username, passwordEncoder.encode(password), role)));
    }

    @Transactional(readOnly = true)
    public List<UserSummary> list() {
        return repository.findAllByOrderByUsernameAsc().stream().map(UserSummary::of).toList();
    }

    @Transactional(readOnly = true)
    public boolean hasAnyUsers() {
        return repository.count() > 0;
    }

    /**
     * Changes role and/or enabled flag. An admin can't demote or disable
     * themselves, so the system can't be left with no one able to manage it.
     */
    @Transactional
    public UserSummary update(String actingUsername, String rawUsername, Role newRole, Boolean enabled) {
        AppUser user = require(rawUsername);
        boolean self = user.getUsername().equals(normalizeUsername(actingUsername));
        if (self && newRole != null && newRole != Role.ADMIN) {
            throw new BadRequestException("You can't remove your own admin role.");
        }
        if (self && Boolean.FALSE.equals(enabled)) {
            throw new BadRequestException("You can't disable your own account.");
        }
        if (newRole != null) {
            user.setRole(newRole);
        }
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        return UserSummary.of(user);
    }

    @Transactional
    public void resetPassword(String rawUsername, String newPassword) {
        requireAcceptablePassword(newPassword);
        require(rawUsername).setPasswordHash(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void changeOwnPassword(String username, String currentPassword, String newPassword) {
        AppUser user = require(username);
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        if (currentPassword.equals(newPassword)) {
            throw new BadRequestException("New password must differ from the current one.");
        }
        requireAcceptablePassword(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    private AppUser require(String rawUsername) {
        String username = normalizeUsername(rawUsername);
        return repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No such user: " + username));
    }

    private static void requireAcceptablePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be " + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH
                    + " characters long.");
        }
        if (password.isBlank()) {
            throw new BadRequestException("Password can't be only whitespace.");
        }
    }
}
