# Solutions: Chapter 35

### Exercise 35.1 ★ Health or metric?

"Is the database reachable?" is a health check (`/actuator/health` returns `503` when it is down). "How many tiles were served today?" is a metric (`sdv_tiles_served_total`, looked at as a rate over a day). "Who opened document X?" is the audit log (`PAGE_VIEWED` events, filtered by document).

### Exercise 35.2 ★★ Read the rule

`fromAddresses(cidrs)` builds one address matcher per configured range and allows the request only if the request's address matches at least one. Judging by the connection address (the TCP peer) matters because `X-Forwarded-For` can be forged (Chapter 32); if the rule believed the header, anyone could claim to be the Prometheus server. In the compose stack nginx never proxies `/actuator/prometheus`, so it is also unreachable from outside.

### Exercise 35.3 ★★★ Design an alert

An example: fire when `increase(sdv_sign_in_total{outcome="locked"}[15m]) > 5` (the throttle window is 15 minutes). First response: open the admin audit log, filter by the lockout event type, read the `rule=` value (account+IP, IP, or account-wide) and the addresses. If one IP: likely guessing, consider blocking it at the network edge. If account-wide: expect the owner may be locked out from a new device, and decide whether to press Unlock after contacting them. Any threshold with a stated time window and a concrete first step earns credit.
