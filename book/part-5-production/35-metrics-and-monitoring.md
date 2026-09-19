<!-- chapter: 35 | part: V | owner: writer-production | tag: book-m5-platform | status: draft -->
# Chapter 35: Health, metrics and alerting

Once the app is running for other people, you can't watch it by staring at a terminal. This chapter shows how the Secure Document Viewer reports its own condition: a health check that says whether it is alive, counters that say what it is doing, and an audit trail that says who did it. You'll also decide which numbers deserve an alert.

## Learning objectives

By the end of this chapter, you will be able to:

- Explain the difference between a health check, a metric, and an audit event.
- Read the project's health endpoint and say why it exposes only a status.
- Name the `sdv_*` counters and say what each one tells you.
- Choose alert conditions for sign-in lockouts, rate limiting, and render rejections.
- Explain why the metrics endpoint is restricted by address.

## Prerequisites

- Chapter 11: Spring Boot foundations (starters, configuration).
- Chapter 33: the compose stack and the proxy in front of the app.

## Beginner tier: Three ways an app talks about itself

### 35.1 The analogy: a car's dashboard

A car has a "check engine" light (is something wrong right now?), gauges (how fast, how hot, how much fuel?), and, in some fleets, a trip log (who drove it, where, when?). The app has all three. The **health check** is the light, **metrics** are the gauges, and the **audit log** is the trip log.

The analogy breaks down because a car's driver sees the dashboard automatically. The app's numbers are invisible until something reads them, which is why this chapter also covers a scraper and alerts.

### 35.2 Terms you need

- **Actuator:** the Spring Boot module that adds operational endpoints such as health and metrics.
- **Health check:** a URL that answers "are you working?" with a status code. Docker and monitors call it.
- **Metric:** a named number measured over time, such as tiles served.
- **Counter:** a metric that only goes up; you look at how fast it rises.
- **Micrometer:** the library Spring Boot uses to record metrics.
- **Prometheus:** a monitoring system that periodically fetches ("scrapes") metrics from a URL and stores them.
- **Alert:** a rule that notifies a person when a metric crosses a threshold.
- **Audit event:** a stored record of a security-relevant action (Chapter 16).

## Intermediate tier: What the app exposes

*On a first read you can skip to "In this project".*

### 35.3 Health checks

`GET /actuator/health` is public and reports status only. It returns `503` when the database is down, so a monitor sees the failure. In the PR #3 live test it showed UP, then DOWN (`503`) when MySQL was stopped, then UP again; `/actuator/env` returned `401`. Every other Actuator endpoint is closed (README).

nginx forwards only this Actuator path (`location = /actuator/health` in `frontend/nginx.conf`), and the compose file uses the same URL for the app container's health check: `curl -fsS http://localhost:8080/actuator/health`. Docker marks the container unhealthy after repeated failures, and the `web` service waits for it before starting.

Why status only? Detailed health output can reveal database names, disk paths, and library versions. A public endpoint should answer yes or no.

### 35.4 Metrics with Micrometer and Prometheus

`GET /actuator/prometheus` serves metrics in Prometheus's text format. In `SecurityConfig` (Listing 32.2) it is not public: the rule

```java
.requestMatchers(HttpMethod.GET, "/actuator/prometheus")
        .access(fromAddresses(properties.getMetricsAllowedAddresses()))
```

allows a request only from the CIDR ranges in `metrics-allowed-addresses` (setting `METRICS_ALLOWED_ADDRESSES`), judged by the real connection address; the default is loopback. nginx never proxies this path, so from outside the compose network it is unreachable. In production you set the setting to your Prometheus server's address.

<!-- source: PR #5 body "Operations"; README "Configuration" -->
The endpoint came from a finding by the Senior Technical Manager review agent (Chapter 32), which asked for operational visibility without exposing it to the world.

### 35.5 The `sdv_*` counters

Besides the usual JVM, HTTP, and connection-pool metrics, the app records its own (README):

**Table 35.1 — The app's own metrics**

| Metric | What it counts | What a change tells you |
|---|---|---|
| `sdv_tiles_served_total` | Tiles delivered | Reading traffic; the baseline for everything else |
| `sdv_tiles_rate_limited_total` | Tile requests refused with `429` | Readers hitting the limit, or a scripted harvest |
| `sdv_sign_in_total{outcome=success\|failure\|locked}` | Sign-in attempts by outcome | A rise in `failure` is guessing; a rise in `locked` means a throttle rule fired |
| `sdv_render_seconds` | Time to render a PDF | Slow or heavy uploads |
| `sdv_render_rejected_total` | Uploads refused because renders were busy or timed out | Not enough render capacity, or pathological PDFs |
| `sdv_render_abandoned_running` | Renders abandoned after timeout but still running | One page holding a render slot (README Limitations) |

## Advanced tier: Deciding what to alert on

*On a first read you can skip to "In this project".*

### 35.6 Alerts

An alert that fires all the time is ignored. The README's go-live checklist names three to start with, and each maps to a threat from Chapter 32:

- **`sdv_sign_in_total{outcome="locked"}`:** someone is guessing passwords, or a real user is locked out. Investigate the audit log for the `rule=` that fired.
- **`sdv_tiles_rate_limited_total`:** either readers find the limit too tight or a client is pulling tiles too fast. The product owner's sign-off on the rate-limit defaults (September 19, 2026) says to revisit them based on this number.
- **`sdv_render_rejected_total`:** uploads are being refused. Capacity is too low, or a publisher is sending files that time out.

Pick thresholds from a week of real traffic, not from guesses; the counters give you the baseline.

### 35.7 The audit log as a third signal

Metrics say something happened; the audit log says who. `PAGE_VIEWED` is recorded once per session, document, and page every 10 minutes rather than per tile, and `ACCESS_DENIED` events are capped per user, so a flood of requests can't fill the table. Each watermark carries a six-character trace code that the audit log's trace filter turns into the exact sign-in (Chapter 32). Events older than 180 days are purged nightly (Chapter 34).

## In this project

| Path | First appears | What it does |
|---|---|---|
| `src/main/java/com/example/securedocviewer/security/SecurityConfig.java` | file from `book-m1-accounts`; public health rule in `book-m3-hardening`; metrics-by-address in `book-m5-platform` | Health public, metrics by address |
| `frontend/nginx.conf` | `book-m5-platform` | Forwards only `/actuator/health` |
| `docker-compose.yml` | `book-m5-platform` | Container health checks |
| `README.md` ("Configuration") | `book-m5-platform` | `metrics-allowed-addresses` and the `sdv_*` list |

## Try it

### Exercise 35.1 ★ Health or metric?

For each, say which signal fits best: "is the database reachable?", "how many tiles were served today?", "who opened document X?"

### Exercise 35.2 ★★ Read the rule

Explain in your own words what `fromAddresses(...)` does, and why judging by the connection address (not `X-Forwarded-For`) matters for this endpoint.

### Exercise 35.3 ★★★ Design an alert

Write an alert for `sdv_sign_in_total{outcome="locked"}`: the threshold, the time window, and what a person should do first when it fires.

## Summary

- Health checks say alive or not; metrics say how much; the audit log says who.
- Health is public but minimal; metrics are restricted by address.
- The `sdv_*` counters map to the threats in Chapter 32.
- Alert on a few signals, with thresholds from real traffic.

Chapter 36 turns to the code and images you don't write yourself: dependencies, scanning, and CI.

## Further reading

- Spring Boot reference, Actuator: https://docs.spring.io/spring-boot/reference/actuator/
- Micrometer documentation: https://docs.micrometer.io/
- Prometheus documentation: https://prometheus.io/docs/
