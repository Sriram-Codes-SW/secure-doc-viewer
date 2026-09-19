<!-- chapter: 33 | part: V | owner: writer-production | tag: book-m5-platform | status: draft -->
# Chapter 33: Deployment and TLS

This chapter moves the Secure Document Viewer from your laptop to a server that other people can reach. You'll learn what changes when strangers are on the other end of the connection, how nginx and Caddy stand in front of the app, and how to read the compose file that ties the pieces together.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain what changes when an app moves from a laptop to a public server.
- Describe the job of a reverse proxy and read the project's nginx configuration.
- Start the app with the `full` and `tls` compose profiles and say what each runs.
- Explain HTTPS, certificates, and HSTS, and why cookies need the `Secure` flag.
- Work through the go-live checklist and justify each item.

## Prerequisites

- Chapter 8: HTTP, headers, cookies (section 8.6 on cookies).
- Chapter 10: Docker images, containers, volumes, and Compose.
- Chapter 30: the platform milestone, where the Docker stack was built.
- Chapter 32: the trust boundary and the forged-address incident.

## Beginner tier: From laptop to server

### 33.1 The analogy: a shop front

On your laptop the app is a workshop: only you walk in. On a server it is a shop on a busy street. You don't let customers walk into the workshop. You put a counter in front, and staff at the counter take requests, check them, and pass them to the back room. A **reverse proxy** is that counter. It receives requests from browsers and forwards them to the app behind it.

The analogy breaks down because the counter here also does jobs a shop counter doesn't: it serves the app's static files itself and, in the HTTPS setup, scrambles all traffic so that people on the street can't read it.

### 33.2 Terms you need

- **Reverse proxy:** a program that accepts requests on behalf of another program and forwards them. This project uses **nginx** (pronounced "engine-x") for this job, and **Caddy** for HTTPS.
- **TLS and HTTPS:** TLS is the protocol that encrypts a connection and proves the server's identity; HTTPS is HTTP carried over TLS. The padlock in a browser means the connection uses it.
- **Certificate:** a file, issued by a trusted authority, that proves a server owns its domain name. Without one, browsers warn users away.
- **Let's Encrypt:** a free authority that issues certificates automatically. Caddy talks to it for you.
- **HSTS:** a header, `Strict-Transport-Security`, that tells a browser "only ever use HTTPS for this site from now on".
- **Compose profile:** a label on a service in the compose file. A service with a profile starts only when you ask for that profile.
- **Non-root:** a process that runs as an ordinary user inside the container, so a break-in there doesn't hand over the whole container.

### 33.3 What changes on a server

Four things change, and each one has a section below:

1. Traffic crosses networks you don't control, so it must be encrypted (sections 33.7 and 33.8).
2. The app must not be reachable except through the front door (section 33.5).
3. The address a request comes from is now the proxy's, so the app must be told the real one, and told whom to believe (section 33.6, Chapter 32).
4. Passwords and keys must come from the environment, not from code (section 33.9).

## Intermediate tier: How the pieces fit

*On a first read you can skip to "In this project"; the compose file is worth reading once you deploy.*

### 33.4 The compose file

The project's `docker-compose.yml` at `book-m5-platform` defines four services. Table 33.1 summarizes them.

**Table 33.1 — Services in `docker-compose.yml`**

| Service | Profile | Image or build | Published port | Role |
|---|---|---|---|---|
| `mysql` | (always) | `mysql:8.4` pinned by digest | `127.0.0.1:3306` | The database |
| `app` | `full` | Built from `Dockerfile` | none | The Spring Boot API |
| `web` | `full` | Built from `frontend/Dockerfile` | `127.0.0.1:8081` | nginx: the Angular app and the `/api` proxy |
| `tls` | `tls` | `caddy:2-alpine` pinned by digest | `127.0.0.1:8443` | HTTPS front end with HSTS |

The profiles give three ways to start it (from the comment at the top of the file):

```bash
docker compose up -d                                        # MySQL only, for development
docker compose --profile full up -d --build                 # MySQL + API + web at http://localhost:8081
docker compose --profile full --profile tls up -d --build   # also https://localhost:8443
```

Notice what is *not* published. `app` uses `expose: "8080"`, which makes the port reachable inside the compose network only. The database is bound to `127.0.0.1`, meaning only the host machine can connect. Only `web` (and `tls`) accept traffic from outside the container network, and even those bind to `127.0.0.1`; putting a real site on the internet means publishing ports 80 and 443 deliberately (section 33.8).

### 33.5 Why not publish the app directly?

Because everything Chapter 32 built assumes traffic arrives through nginx. nginx sets the security headers for the frontend, caps upload size, and, above all, overwrites `X-Forwarded-For` with the real connection address. If `app:8080` were reachable directly, a caller could bypass those steps. The README states it as a rule: keep the API reachable only through the proxy.

### 33.6 The nginx configuration, piece by piece

**Listing 33.1 — `frontend/nginx.conf`, `book-m5-platform` (simplified: the static-asset location, the health-check location, and several comments are omitted)**

```nginx
server {
    listen 8080;
    server_name _;
    server_tokens off;

    set_real_ip_from 172.28.0.11;
    real_ip_header X-Forwarded-For;

    root /usr/share/nginx/html;
    index index.html;

    client_max_body_size 51m;

    location ^~ /api/ {
        proxy_pass http://app:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $remote_addr;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $forwarded_proto;
        proxy_read_timeout 300s;
        proxy_request_buffering off;
    }

    location / {
        add_header Content-Security-Policy "default-src 'self'; script-src 'self'; ..." always;
        try_files $uri $uri/ /index.html;
    }
}
```

Line by line:

- `listen 8080` and the unprivileged nginx image: nginx runs as a non-root user, which cannot bind to ports below 1024, hence 8080 inside the network.
- `server_tokens off` hides nginx's version number in responses.
- `set_real_ip_from 172.28.0.11` and `real_ip_header X-Forwarded-For`: accept a forwarded client address only from Caddy at that fixed address. Anyone else's header is ignored.
- `client_max_body_size 51m` is kept in step with the backend's 50 MB upload limit plus form overhead.
- `location ^~ /api/` proxies API calls to `app:8080`. The `^~` marks it so that no pattern rule elsewhere can capture an API path.
- `proxy_set_header X-Forwarded-For $remote_addr` is the fix for the incident in Chapter 32: nginx *overwrites* the header with the real connection address instead of appending to what the client sent.
- `proxy_read_timeout 300s` allows for a long PDF render; `proxy_request_buffering off` streams uploads through instead of holding them.
- The final `location /` serves the Angular app with its own strict Content-Security-Policy: scripts and styles from the same origin only, tiles allowed as `blob:` images, nothing may frame the viewer. Everything the browser needs comes from one origin, so the session and CSRF cookies work without extra settings.

### 33.7 Caddy and certificates

**Listing 33.2 — `deploy/Caddyfile`, `book-m5-platform` (simplified: comments omitted)**

```
{$SITE_ADDRESS} {
	tls {$TLS_MODE}
	encode zstd gzip
	header {
		Strict-Transport-Security "{$HSTS_POLICY:max-age=31536000}"
		-Server
	}
	reverse_proxy web:8080
}
```

Caddy reads three settings from the environment. `SITE_ADDRESS` is the host name (default `localhost`). `TLS_MODE` is `internal` to use Caddy's own local certificate authority, fine for trying it out (browsers warn until you trust it), or an email address to get a real certificate from Let's Encrypt, which needs a public DNS name and ports 80 and 443 reachable. `HSTS_POLICY` defaults to `max-age=31536000`, one year. `reverse_proxy web:8080` forwards everything to nginx, and Caddy replaces `X-Forwarded-For` with the real client address, because no proxy sits in front of it.

Why two proxies? nginx already serves the app. Caddy's strength is certificates: it obtains and renews them without configuration scripts. Keeping it as an optional profile means the plain stack stays simple. Chapter 37 (section 37.11) compares this with a cloud load balancer.

## Advanced tier: Trust, cookies, and going live

*On a first read you can skip to "In this project"; return here before a real deployment.*

### 33.8 Fixed addresses and the trust boundary

The compose file gives the network a fixed subnet, `172.28.0.0/24`, `web` the address `172.28.0.10`, and `tls` the address `172.28.0.11`. This is not decoration. The app sets `FORWARD_HEADERS_STRATEGY: native` and `TRUSTED_PROXY_REGEX: '172\.28\.0\.10'`, so it believes `X-Forwarded-For` only from nginx; nginx believes a forwarded address only from Caddy. The chain of belief is one link at a time, and every link is pinned to an address that cannot change.

<!-- source: dossier/decisions.md D11, D12; PR #5 body "Operations" (TM2-6) -->
The fixed subnet came from a finding by the Senior Technical Manager review agent (Chapter 32): without pinned addresses, "trust the proxy" could not be expressed safely. The 18-check live test in Chapter 32 verified the chain.

### 33.9 Secrets and configuration

Secrets are never in the compose file. The file uses `${DB_PASSWORD:?Set DB_PASSWORD in .env}`, which makes Compose refuse to start when the value is missing, and `env_file: .env` passes the rest to the app. The `.env` file is git-ignored; `.env.example` is the template. `SIGNING_SECRET` (at least 32 characters; startup fails otherwise) keys every tile token. The MySQL health check uses `$$MYSQL_ROOT_PASSWORD` so the password never appears in the stored command that `docker inspect` shows.

### 33.10 Secure cookies and HSTS

A cookie marked `Secure` is sent only over HTTPS. Set `SESSION_COOKIE_SECURE=true` wherever the app is served over HTTPS; the CSRF cookie then carries `Secure` too (the live test confirmed this behind Caddy). HSTS closes the remaining gap: once a browser has seen the header, it refuses to use plain HTTP for the site for a year. That is also why `includeSubDomains` is opt-in through `HSTS_POLICY` (commits `66f7152`, `5aa0f3c`): adding it commits every subdomain to HTTPS, and a browser will remember the promise.

### 33.11 Non-root, memory limits, health checks

`app` runs as a system user named `app` (in `Dockerfile`, `USER app`). `web` uses the `nginx-unprivileged` image. The containers have memory limits (`mem_limit: 1536m` for the app, whose JVM sizes its heap to 75% of that; 128m for nginx and Caddy). Each service has a health check, and `depends_on` with `condition: service_healthy` makes them start in order: database, then app, then web, then Caddy.

### 33.12 The go-live checklist

From the README, with the reason for each:

- **Serve over HTTPS** with the `tls` profile, `SITE_ADDRESS` set to your domain, `TLS_MODE` set to an email address, ports 80 and 443 published, and `SESSION_COOKIE_SECURE=true`. Reason: passwords and session cookies must not cross the internet in the clear.
- **Keep `app:8080`, MySQL, and the metrics endpoint off the network**, and set `METRICS_ALLOWED_ADDRESSES` to the Prometheus server. Reason: these are behind the front door, not on it.
- **Strong, unique `SIGNING_SECRET`, `DB_PASSWORD`, and `DB_ROOT_PASSWORD`**, and change the bootstrap admin password at first sign-in. The app forces this only when it generated the password; if you set `BOOTSTRAP_ADMIN_PASSWORD`, change it yourself and clear it from `.env`.
- **Scheduled backups plus one restore drill** (Chapter 34).
- **One app instance** (Chapter 34.5, Chapter 37).

## In this project

| Path | First appears | What it does |
|---|---|---|
| `docker-compose.yml` | `book-m1-accounts` (MySQL), extended in `book-m5-platform` | Services, profiles, fixed subnet |
| `Dockerfile` | `book-m5-platform` | Two-stage backend image, non-root runtime |
| `frontend/Dockerfile` | `book-m5-platform` | Angular build, then unprivileged nginx |
| `frontend/nginx.conf` | `book-m5-platform` | Proxy, headers, header overwrite |
| `deploy/Caddyfile` | `book-m5-platform` | TLS and HSTS |
| `.env.example` | `book-m1-accounts` | Template for secrets (never commit `.env`) |

See any with `git show book-m5-platform:<path>`.

## Try it

### Exercise 33.1 ★ Read the ports

List every port that `docker compose --profile full --profile tls up` publishes on the host, and the address each is bound to. Explain why none is `0.0.0.0`.

### Exercise 33.2 ★★ Predict the spoof

A client sends `X-Forwarded-For: 203.0.113.9` directly to nginx on port 8081. Using Listing 33.1, what address does the app see, and why?

### Exercise 33.3 ★★★ Change the address

Suppose you change nginx's address in the compose file to `172.28.0.20` and change nothing else. Which settings stop working, and what is the visible symptom?

## Summary

- A reverse proxy is the app's front door; here nginx serves the frontend and proxies `/api`, and Caddy adds HTTPS.
- Only the proxies publish ports; the API and database stay inside the network.
- Trust is a chain of fixed addresses: app trusts nginx, nginx trusts Caddy.
- `Secure` cookies and HSTS keep sessions off plain HTTP.
- The go-live checklist turns these ideas into steps.

Chapter 34 covers backups, restores, and living with one instance.

## Further reading

- nginx documentation: https://nginx.org/en/docs/
- Caddy documentation: https://caddyserver.com/docs/
- Docker Compose profiles: https://docs.docker.com/compose/how-tos/profiles/
- MDN, Strict-Transport-Security: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Strict-Transport-Security
