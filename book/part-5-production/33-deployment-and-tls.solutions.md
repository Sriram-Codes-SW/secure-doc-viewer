# Solutions: Chapter 33

### Exercise 33.1 ★ Read the ports

`mysql`: `127.0.0.1:3306` (configurable with `DB_PORT`). `web`: `127.0.0.1:8081` (`WEB_PORT`), mapped to nginx's 8080 inside the network. `tls`: `127.0.0.1:8443` (`TLS_PORT`), mapped to Caddy's 443. `app` publishes nothing; it only `expose`s 8080 to the compose network. None is `0.0.0.0` because each mapping starts with `127.0.0.1:`, so only the host machine itself can connect. Publishing on all interfaces is a deliberate go-live step (ports 80 and 443).

### Exercise 33.2 ★★ Predict the spoof

The app sees nginx's view of the TCP peer, not the client's header. In the `/api/` location nginx runs `proxy_set_header X-Forwarded-For $remote_addr;`, which overwrites the header. Also, `set_real_ip_from` trusts a forwarded address only from `172.28.0.11` (Caddy), so a client connecting directly is not believed. The app receives the client's real connection address (the Docker gateway address in a local setup), never `203.0.113.9`.

### Exercise 33.3 ★★★ Change the address

`TRUSTED_PROXY_REGEX` still names `172\.28\.0\.10`, so the app no longer trusts nginx's forwarded header: it judges every request by its own peer address, which is now nginx's. Symptom: every user appears to come from the same address, so per-IP throttling and the audit log's addresses all show nginx, and one user's failed sign-ins count against everyone (the 20-per-IP rule locks out all users sooner). Also, Caddy's fixed address `172.28.0.11` in nginx's `set_real_ip_from` is unaffected, but `ipv4_address` for `web` must still sit in the `172.28.0.0/24` subnet. The fix is to change the compose address and `TRUSTED_PROXY_REGEX` together.
