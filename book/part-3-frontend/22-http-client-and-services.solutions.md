<!-- chapter: 22 | part: III | owner: writer-frontend | solutions -->
# Chapter 22 solutions

### Exercise 22.1 ★ The HTTP methods

`GET` (list, get, findUsers, getTileUrls), `POST` (upload), `PATCH` (update), `PUT` (replaceFile, transferOwnership, share), and `DELETE` (delete, unshare). The file is sent by `upload` (`POST`, multipart `FormData`) and by `replaceFile` (`PUT`).

### Exercise 22.2 ★ The interceptor and a 401

Nothing special: `/api/auth/login` is in `AUTH_PROBES`, so the 401 is passed to the login form's own error handler, which shows "Incorrect username or password." Redirecting to `/login` would be pointless (the reader is already there) and would wipe what they typed.

### Exercise 22.3 ★★ Find the CSRF header

Yes: the header `X-XSRF-TOKEN` carries the same value as the `XSRF-TOKEN` cookie. A page on another origin can't read that cookie, so it can't produce the header.

### Exercise 22.4 ★★ The fallback retry time

It applies only when a 429 or 503 arrives without a usable `Retry-After` header: the viewer then waits that many seconds (5 by default) before asking for fresh tile URLs.

### Exercise 22.5 ★★★ A reload without the version check

If the reload returned the same version that was reported gone (for example because the tiles were unavailable for another reason), the viewer would reload, refetch the tiles, receive 410 again, and repeat indefinitely, hammering the server and spending the rate-limit budget. The check stops after one reload and shows an error instead.

### Exercise 22.6 ★★ Why `switchMap`

The reader types "al" and a request for "al" starts, and the server is slow to answer. The reader types "alice" and a request for "alice" starts, and the server answers it quickly. With `mergeMap`, both requests stay alive; when the slow answer for "al" finally arrives, it is emitted after the answer for "alice", replacing the correct suggestions with the ones for "al". `switchMap` cancels the "al" request as soon as "alice" starts, so a stale answer can never arrive.
