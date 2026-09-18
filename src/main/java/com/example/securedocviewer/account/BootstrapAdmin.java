package com.example.securedocviewer.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Creates the first admin on an empty database — otherwise nobody could sign
 * in to create anyone else. The password comes from BOOTSTRAP_ADMIN_PASSWORD;
 * if that isn't set, a random one is generated and logged once (the same
 * approach Spring Boot takes for its default user), so no credential ever has
 * to be committed. Does nothing once any account exists.
 */
@Component
public class BootstrapAdmin implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdmin.class);
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final UserAccountService accounts;
    private final String username;
    private final String configuredPassword;

    public BootstrapAdmin(UserAccountService accounts,
                          @Value("${secure-doc-viewer.bootstrap-admin.username:admin}") String username,
                          @Value("${secure-doc-viewer.bootstrap-admin.password:}") String configuredPassword) {
        this.accounts = accounts;
        this.username = username;
        this.configuredPassword = configuredPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (accounts.hasAnyUsers()) {
            return;
        }
        boolean generated = configuredPassword == null || configuredPassword.isBlank();
        String password = generated ? randomPassword() : configuredPassword;
        accounts.create(username, password, Role.ADMIN);
        if (generated) {
            log.warn("\n\nCreated initial admin account '{}' with generated password: {}\n"
                    + "Sign in and change it (or set BOOTSTRAP_ADMIN_PASSWORD before first start).\n", username, password);
        } else {
            log.info("Created initial admin account '{}' from BOOTSTRAP_ADMIN_PASSWORD.", username);
        }
    }

    private static String randomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            password.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return password.toString();
    }
}
