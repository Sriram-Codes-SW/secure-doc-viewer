<!-- chapter: 15 | part: II | owner: writer-backend | tag: book-m1-accounts | status: draft -->
# Chapter 15: Spring Security I: who are you?

Before the Secure Document Viewer can decide what you may open, it must know who you are. This chapter covers authentication: how passwords are stored, how a session cookie keeps you signed in, how roles are assigned, and why the sign-in endpoint gives the same answer for a wrong password and an unknown user.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain the difference between authentication and authorization.
- Describe what a servlet filter is and how Spring Security's filter chain uses it.
- Explain why passwords are hashed and salted, what BCrypt is, and its 72-byte limit.
- Explain how a session cookie works and what `httpOnly` and `SameSite` do.
- Read `DatabaseUserDetailsService` and the three roles.
- Explain why sign-in reveals nothing about which accounts exist.

**A note on versions.** The chapter belongs to milestone 1 (`book-m1-accounts`), but the listings are labeled with the tag they were copied from. `Role.java` and `DatabaseUserDetailsService.java` are identical at `book-m1-accounts` and `book-m6-final`; the password encoder bean has the same logic at both. The 72-byte check (`fitsBcrypt`), the sign-in code in `AuthController` and the `SecurityConfig` chain shown in Listing 15.1 are quoted from `book-m6-final`: at `book-m1-accounts` the `fitsBcrypt` check does not exist yet, and those files differ.

## Prerequisites

- Chapter 8: how the web works (cookies, headers)
- Chapter 11: Spring Boot foundations
- Chapter 12: REST controllers and JSON
- Chapter 14: Storing data with JPA and Flyway

## Beginner tier: Proving who you are

### 15.1 Authentication, authorization, and the difference

**Authentication** answers "who are you?" **Authorization** answers "what may you do?" Think of a building with a front desk. Showing your badge to the guard is authentication; the badge reader that opens only some doors is authorization. This chapter is the guard; [Chapter 16](16-spring-security-defenses.md) is the doors.

**Where the analogy breaks down.** A guard sees your face. A server sees only a claim (a username and password) and must check it every time, because requests carry no memory of earlier ones unless you add one (Section 15.4).

**Spring Security** is the framework module that does both jobs. You add `spring-boot-starter-security`, and Spring Boot 4 with Spring Security 7 protects every endpoint until you configure otherwise.

### 15.2 The filter chain

A **servlet filter** is a piece of code that every request passes through before it reaches a controller, and every response passes back through. Spring Security is a chain of such filters, each with one job: read the session, check a CSRF token, decide whether the request is allowed. You describe the chain in one bean. The project's begins like this (Listing 15.1).

**Listing 15.1 — `SecurityConfig.java` (`book-m6-final`, simplified: only the opening lines and the last disabled features are shown)**

*`src/main/java/com/example/securedocviewer/security/SecurityConfig.java`*

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, /* ... */) throws Exception {
        http
                // ... csrf, sessions, headers, authorization rules, custom filters ...
                .requestCache(cache -> cache.disable())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }
```

`@Configuration` marks a class whose `@Bean` methods create beans by hand. Here the bean is the chain itself. The project turns off Spring's built-in HTML login form, HTTP Basic and logout, because the Angular app sends JSON to its own `AuthController` (`/api/auth/login`). The middle of the chain, the CSRF and authorization rules, is Chapter 16.

### 15.3 Storing passwords: hashing, salting, BCrypt, the 72-byte limit

The database must never contain passwords, because anyone who reads it (a backup, a leaked disk) would have every account. It stores a **hash**: the output of a one-way function that turns a password into a fixed-length string that can't practically be reversed. To check a login, hash what was typed and compare.

Two people with the same password would have the same hash, and attackers precompute hashes of common passwords. A **salt** is random data mixed in per password, so identical passwords hash differently. **BCrypt** is a hashing function built for passwords: it includes the salt in its output and is deliberately slow, which makes guessing billions of passwords expensive. Listing 15.2 is the project's encoder bean.

**Listing 15.2 — `SecurityConfig.passwordEncoder` (`book-m6-final`; identical logic at `book-m1-accounts`)**

*`src/main/java/com/example/securedocviewer/security/SecurityConfig.java`*

```java
/** Delegating encoder stores "{bcrypt}..." so the algorithm can be upgraded later without a migration. */
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

The delegating encoder writes a prefix such as `{bcrypt}` before the hash, so a future version can add a stronger algorithm while old hashes still verify. The hash column is `VARCHAR(100)` (migration `V1`).

BCrypt has a limit worth knowing: it reads only the first 72 bytes of a password and refuses longer input. `UserAccountService` handles this explicitly:

```java
/** BCrypt uses at most 72 bytes of a password and refuses longer ones. */
public static final int MAX_PASSWORD_BYTES = 72;

public static boolean fitsBcrypt(String password) {
    return password == null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
}
```

(`book-m6-final`, `UserAccountService.java`.) The check counts bytes, not characters: an accented letter or emoji takes several bytes, so a short-looking password can be too long. The rule is enforced when a password is set and again at sign-in, so an over-long attempt is refused cleanly instead of failing inside BCrypt.

## Intermediate tier: Sessions, roles and users

### 15.4 Sessions and the session cookie (`SDV_SESSION`, httpOnly, SameSite)

HTTP forgets you between requests. After a successful sign-in, the server creates a **session**: a record on the server tied to a random id, which it sends to the browser in a **cookie**. The browser returns the cookie on every later request, and the server looks up who you are. The project configures it in `application.yml` (`book-m6-final`):

```yaml
cookie:
  name: SDV_SESSION
  http-only: true
  same-site: strict
  secure: ${SESSION_COOKIE_SECURE:false}
```

- `http-only: true`: JavaScript can't read the cookie, so a script injected into the page can't steal the session id.
- `same-site: strict`: the browser sends it only for requests that start on the app's own site, which blocks a whole class of cross-site tricks (Chapter 16).
- `secure`: send it only over HTTPS. It's `false` for local development and set to `true` wherever the app is served over HTTPS (the file's comment says it "Must be true anywhere the app is served over HTTPS").

The alternative is a **token** (for example a JWT) that the browser stores and sends in a header. The project chose the server-side session because it can be ended instantly by the server (Chapter 16), and because the id stays out of reach of JavaScript. `AuthController`'s class comment states the property: the response "never contains the session id: it travels only in the httpOnly session cookie set by the container."

### 15.5 Roles: READER, PUBLISHER, ADMIN

A **role** is a named set of permissions attached to an account. Listing 15.3 is the whole definition.

**Listing 15.3 — `Role.java` (`book-m1-accounts`, identical at `book-m6-final`)**

*`src/main/java/com/example/securedocviewer/account/Role.java`*

```java
public enum Role {
    READER,
    PUBLISHER,
    ADMIN
}
```

Its comment explains them: every signed-in user can read documents they have access to, publishers can also upload, and admins can additionally manage accounts, sessions and the audit log. Chapter 16 shows where these are enforced.

### 15.6 Loading users from the database

Spring Security needs one thing from you: given a username, return the account's stored hash and roles. That's a `UserDetailsService` (Listing 15.4).

**Listing 15.4 — `DatabaseUserDetailsService.java` (`book-m1-accounts`, identical at `book-m6-final`; imports omitted)**

*`src/main/java/com/example/securedocviewer/security/DatabaseUserDetailsService.java`*

```java
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository repository;

    public DatabaseUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = repository.findByUsername(UserAccountService.normalizeUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
}
```

This uses the repository from Chapter 14. The username is normalized to lower case first, so `Alice` and `alice` are one account. `.roles("ADMIN")` becomes the authority `ROLE_ADMIN` (Spring adds the prefix). `.disabled(...)` makes a disabled account fail authentication. The `AuthenticationManager` bean in `SecurityConfig` connects this service to the password encoder through a `DaoAuthenticationProvider`; it compares the typed password with the stored hash.

## Advanced tier: Answers that reveal nothing

### 15.7 Same answer for wrong password and unknown user

If the server said "no such user" for one case and "wrong password" for another, an attacker could test a list of names and learn which accounts exist. `AuthController` collapses every authentication failure into one message:

```java
} catch (AuthenticationException e) {
    // Same message for unknown user, wrong password and disabled
    // account, so the response doesn't reveal which accounts exist.
    // The failure was already counted by reserve().
    metrics.signIn(SignInOutcome.FAILURE);
    audit.record(AuditEventType.SIGN_IN_FAILED, attempted, Subject.none());
    throw new BadCredentialsException("Invalid username or password.");
}
```

(`book-m6-final`, `AuthController.java`, excerpt.) `GlobalExceptionHandler` turns that into a `401` with the single message `Invalid username or password.` The test `wrongPasswordAndUnknownUserGetTheSameAnswer` in `SecurityIntegrationTest` sends both kinds of request and asserts that the two response bodies are equal. The server still records the failure in the audit log (Chapter 14), so operators see what attackers can't.

We simplify here: an attacker could still compare response times, and the full defenses, throttling and lockout, are in Chapter 16.

## In this project

**Table 15.1 — Where Chapter 15's ideas live**

| Idea | File | Tag |
|---|---|---|
| Filter chain, encoder | `security/SecurityConfig.java` | `book-m1-accounts`, `book-m6-final` |
| Roles | `account/Role.java` | `book-m1-accounts` |
| User lookup | `security/DatabaseUserDetailsService.java` | `book-m1-accounts` |
| Sign-in endpoint | `controller/AuthController.java` | `book-m6-final` |
| Account rules, BCrypt limit | `account/UserAccountService.java` | `book-m6-final` |
| Session cookie settings | `src/main/resources/application.yml` | `book-m6-final` |

## Try it

1. (★) Which annotation makes the session cookie unreadable to JavaScript? Which file sets it?
2. (★) What does the database store instead of a password?
3. (★★) A password of 30 emoji is 30 characters. Will `fitsBcrypt` accept it? Why?
4. (★★) What is the role string Spring builds from `.roles("PUBLISHER")`?
5. (★★★) Compare a session cookie with a token stored in the browser. Give one advantage of each and say which the project chose and why.

## Summary

- Authentication proves who you are; authorization decides what you may do.
- Spring Security is a chain of filters described by one `SecurityFilterChain` bean.
- Passwords are stored as salted BCrypt hashes, with a delegating prefix; BCrypt accepts at most 72 bytes.
- A server-side session and an `httpOnly`, `SameSite=Strict` cookie keep you signed in without exposing the session id to scripts.
- Three roles, READER, PUBLISHER and ADMIN, are loaded from the database by `DatabaseUserDetailsService`.
- Every authentication failure gets the same response, so accounts can't be enumerated.

## Further reading

- *Spring Security Reference Documentation*, "Authentication." https://docs.spring.io/spring-security/reference/servlet/authentication/index.html
- *Spring Security Reference Documentation*, "Password Storage." https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
- *OWASP Cheat Sheet Series*, "Password Storage Cheat Sheet." https://cheatsheetseries.owasp.org/
- *OWASP Cheat Sheet Series*, "Session Management Cheat Sheet." https://cheatsheetseries.owasp.org/
