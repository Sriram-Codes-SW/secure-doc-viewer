<!-- chapter: 15 | part: II | owner: writer-backend | tag: book-m1-accounts | status: expanded -->
# Solutions: Chapter 15

### Exercise 15.1 ★ Find the cookie settings

`http-only: true` under `server.servlet.session.cookie` in `application.yml` makes the session cookie unreadable to JavaScript. It is configuration, not an annotation. The idle timeout is `server.servlet.session.timeout`, set to `30m`: after 30 minutes without a request the session ends.

### Exercise 15.2 ★ What does the database store?

The column is `password_hash VARCHAR(100) NOT NULL` in `app_user`. It holds the delegating encoder's output: a label such as `{bcrypt}` (8 characters) followed by a BCrypt hash, which is 60 characters. That is 68 characters in total, so 100 leaves room for a longer label or a future algorithm's output. It never holds the password itself.

### Exercise 15.3 ★★ Count bytes, not characters

A minimal program, with the method copied from Listing 15.3 so that it needs nothing from the project:

```java
import java.nio.charset.StandardCharsets;

public class Bytes {
    static final int MAX_PASSWORD_BYTES = 72;

    static boolean fitsBcrypt(String password) {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }

    public static void main(String[] args) {
        System.out.println(fitsBcrypt("a".repeat(72)));
        System.out.println(fitsBcrypt("a".repeat(73)));
        System.out.println(fitsBcrypt("é".repeat(30)));
        System.out.println(fitsBcrypt("😀".repeat(20)));
    }
}
```

Save it as `Bytes.java`; you can run it with `java Bytes.java` (Chapter 3).

Results: `true`, `false`, `true`, `false`. Seventy-two letters are 72 bytes (one byte each in UTF-8) and fit; 73 are one byte over. Thirty copies of `é` are 60 bytes (two bytes each) and fit even though they look like fewer characters than they are bytes. Twenty emoji are 80 bytes (four bytes each) and don't fit, although 20 characters seems short. If you use a different emoji, check that it's a single four-byte character; some emoji are sequences of several code points and take more. The lesson is that the limit is in bytes.

### Exercise 15.4 ★★ Trace a failed sign-in

For an existing user with a wrong password: `login` normalizes the username; `knownDevices.isRecognised`; `loginThrottle.reserve` (counts the attempt); `UserAccountService.fitsBcrypt`; `authenticationManager.authenticate`, which calls `DatabaseUserDetailsService.loadUserByUsername`, then the encoder's `matches`, which returns false, so an `AuthenticationException` (a `BadCredentialsException`) is thrown; the `catch` block records a failure metric and a `SIGN_IN_FAILED` audit event, and throws a new `BadCredentialsException("Invalid username or password.")`. `GlobalExceptionHandler.handleBadCredentials` turns it into `401` with `{"error": "Invalid username or password."}`.

For a user that doesn't exist: the path is the same up to `loadUserByUsername`, which throws `UsernameNotFoundException`. Spring Security's provider treats this as an authentication failure (by default it hides the "not found" detail by reporting bad credentials), so the same `catch` block runs. The two paths meet at the `catch (AuthenticationException e)`, and everything after it, including the response, is identical.

### Exercise 15.5 ★★★ Sessions or tokens?

A worked outline.

- **Current design.** `UserAdminController.resetPassword` and `AuthController.changePassword` call `sessions.revokeAllFor(...)`, which expires every session of that user in the `SessionRegistry`. Spring Security then rejects each of those sessions on its very next request. Because the server owns the record, "immediately" is simple.
- **Token-only design.** A signed token stays valid until it expires and needs no server lookup, so nothing tells the server the token should now be refused. To add "sign out everywhere" you would need server-side state anyway: a list of revoked tokens, or a per-user "tokens issued before this time are invalid" value checked on every request. That is most of the server-side state a session already gives you.
- **When tokens win.** When many independent servers, or a third party, must verify identity without contacting a central store, or when the client isn't a browser and can't keep cookies. Switching would give up instant revocation, the `httpOnly` protection from scripts, and the simple admin session list. It would also make it necessary to protect the token in the browser, which scripts can read if it's kept in storage.

### Exercise 15.6 ★★★ Design a safe response

This is a design outline, not code from the project; the project has no email feature. One good answer: respond with the *same* status and message whether or not the address is registered, for example `202 Accepted` with "If that address has an account, we've sent a link." Do the work of sending an email in the background, after the response has been returned, so that the time to answer doesn't depend on whether an account exists (looking up and mailing take longer than not finding anyone). Rate-limit the endpoint per address and per client, as `LoginThrottle` does for sign-in, so it can't be used to send a flood of email or to probe addresses at speed. Key decisions: identical bodies and status codes, comparable timing, and a throttle that doesn't reveal by its own behavior which addresses are known.
