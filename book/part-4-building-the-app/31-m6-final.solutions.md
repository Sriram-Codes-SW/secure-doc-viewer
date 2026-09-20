<!-- chapter: 31 | part: IV | owner: writer-app | tag: book-m6-final | status: expanded -->
# Solutions for Chapter 31

### Exercise 31.1 ★ Read the range

- `^5.0.1` allows 5.0.1 up to but not including 6.0.0. Version 5.4.0: yes. Version 6.0.5: no.
- `~6.0.2` allows 6.0.2 up to but not including 6.1.0. Version 5.4.0: no. Version 6.0.5: yes.
- `5.0.1` (no prefix) means exactly that version. Neither 5.4.0 nor 6.0.5 is allowed.

### Exercise 31.2 ★ Why the polling loop does not slow a passing test

The loop checks the condition first and sleeps only while the condition is false. In the usual order of
events the slot is already free on the first check, or after one 10 ms sleep. The 5-second deadline
matters only when something is actually wrong.

### Exercise 31.3 ★★ Peer range

First find which package declares the range (here `@angular/build` declares TypeScript `>=6.0 <6.1`) and
whether the update falls outside it. Two acceptable outcomes: close or defer the update until the
framework upgrade that widens the range, or upgrade the framework and the dependency together in one
planned change. Forcing the install past the range is not acceptable.

### Exercise 31.4 ★★ Spot the race

Two requests can both run the first line before either reaches the third, because the password check
takes about 100 ms. Both see fewer than 5 failures and both proceed, so a burst of parallel guesses
exceeds the limit. To fix it, make the check and the count one atomic step before the slow work: reserve an
attempt (increment) under a lock or with an atomic operation, check the password, and give the
attempt back only when the password is correct. `LoginThrottle.reserve` does this with `synchronized`.

### Exercise 31.5 ★★ Characters and bytes

30 characters at 4 bytes each is 120 bytes. The password passes the 12 to 128 character rule but
fails `fitsBcrypt`, because 120 is more than `MAX_PASSWORD_BYTES` (72). The user sees a 400 error with
the message "Password is too long: at most 72 bytes (fewer characters if it uses accents, non-Latin
letters or emoji)."

### Exercise 31.6 ★★★ Draft a policy

One good answer, modeled on the project's MySQL rule:

```yaml
ignore:
  # Stay on <library> major N: moving to N+1 is a planned upgrade with a migration test.
  - dependency-name: <library>
    update-types: ['version-update:semver-major']
```

On the day the current major stops being supported: read the migration guide, upgrade in its own
pull request on a branch, run the full suite including any integration tests that touch stored data,
take a backup first, remove or update the ignore rule with the reason changed, and record the
decision in the pull request description.
