# Solutions: Chapter 33

### Exercise 33.1 ★ Read the ports

`mysql`: `127.0.0.1:3306` (configurable with `DB_PORT`). `web`: `127.0.0.1:8081` (`WEB_PORT`), mapped to nginx's 8080 inside the network. `tls`: `127.0.0.1:8443` (`TLS_PORT`), mapped to Caddy's 443. `app` publishes nothing; it only `expose`s 8080 to the compose network. None is `0.0.0.0` because each mapping starts with `127.0.0.1:`, so only the host machine itself can connect. Publishing on all interfaces is a deliberate go-live step: you edit the `tls` service's `ports` entry.

### Exercise 33.2 ★★ Predict the spoof

The app sees the real connection address, never `203.0.113.9`. In the `/api/` location nginx runs `proxy_set_header X-Forwarded-For $remote_addr;`, which overwrites whatever the client sent with the address of the TCP peer. Also, `set_real_ip_from 172.28.0.11` trusts a forwarded address only from Caddy, so a client connecting directly to nginx is not believed. In a local setup the address the app sees is the Docker gateway's address.

### Exercise 33.3 ★★★ Change the address

`TRUSTED_PROXY_REGEX` still names `172\.28\.0\.10`, so the app no longer trusts the forwarded header from nginx at its new address; it judges every request by its own peer address, which is now nginx's. Symptom: every user appears to come from the same address, so per-address throttling and the audit log's addresses all show nginx, and one person's failed sign-ins count against everyone (the 20-per-address rule then locks out all users sooner). Nothing crashes, which is why the failure is silent. Also, the new address must lie inside the `172.28.0.0/24` subnet declared in the compose file. The fix is to change the compose `ipv4_address` and `TRUSTED_PROXY_REGEX` together (and `set_real_ip_from` too, if the change was to Caddy's address).

### Exercise 33.4 ★★ Read the Dockerfile

Docker caches each instruction as a layer and reuses a layer only if the instruction and everything before it are unchanged. Copying `pom.xml` and running `dependency:go-offline` first means the dependency download layer is reused until `pom.xml` changes. If you copied `src` first, any code edit would invalidate the cache from that point on and every build would download all dependencies again, which is much slower.

### Exercise 33.5 ★★ Diagnose

A reasonable order: (1) Are you actually using HTTPS end to end? A `Secure` cookie isn't stored or sent over plain HTTP; open the site by its `https://` address and check the browser's developer tools (Application, Cookies) for the `SDV_SESSION` cookie. (2) Does the app see the request as HTTPS? Check that `FORWARD_HEADERS_STRATEGY` and `TRUSTED_PROXY_REGEX` are set so that `X-Forwarded-Proto` from the proxy is believed. (3) Is something dropping the cookie: a proxy stripping `Set-Cookie`, a different hostname between requests, or a session lifetime or idle timeout ending the session (`session-max-lifetime`, 30-minute idle timeout)? Also check the health of the app (a restart signs everyone out, Chapter 34). Any ordered list that starts with the cheapest, most likely checks earns credit.

### Exercise 33.6 ★★★ Plan the go-live

A model answer. `.env`: strong unique `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `SIGNING_SECRET` (32+ characters), `SITE_ADDRESS=docs.example.com`, `TLS_MODE=<an email address>`, `SESSION_COOKIE_SECURE=true`, `METRICS_ALLOWED_ADDRESSES` set to the monitoring server, and the bootstrap admin password either unset (generated, read from the log once) or set and then cleared. Compose: publish 80 and 443 on the `tls` service. DNS: an A (and AAAA if used) record pointing the name at the server. First three tests: (1) `https://docs.example.com` loads with a valid certificate and the response has `Strict-Transport-Security`; (2) sign in as the admin, change the password, create a reader and confirm they can open a shared document; (3) from another machine, confirm that ports 3306 and 8080 are unreachable and that `/actuator/prometheus` is refused. Then schedule the first backup and restore drill (Chapter 34).
