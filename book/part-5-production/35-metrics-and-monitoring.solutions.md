# Solutions: Chapter 35

### Exercise 35.1 ★ Health, metric, or audit?

"Is the database reachable?" is a health check (`/actuator/health` answers `503` when it is down). "How many tiles were served today?" is a metric (`sdv_tiles_served_total`, looked at as an increase over a day). "Who opened document X?" is the audit log (`PAGE_VIEWED` events, filtered by document). "Are uploads being refused?" is a metric (`sdv_render_rejected_total`), with the audit log available afterward if you need to know which publisher was affected.

### Exercise 35.2 ★ Read the page

Average render time is `sdv_render_seconds_sum / sdv_render_seconds_count` = 118.6 / 31, which is about 3.8 seconds per PDF. The page shows 9 failed sign-ins (`sdv_sign_in_total{outcome="failure"}`), out of 214 + 9 + 0 = 223 attempts.

### Exercise 35.3 ★★ Read the rule

`fromAddresses(cidrs)` builds one address matcher per configured range and allows the request only if the request's address matches at least one. It judges by the connection address because `X-Forwarded-For` can be forged (Chapter 32); a rule that believed the header would let any caller claim to be the Prometheus server. If the setting stays at its default (loopback only) while Prometheus runs in another container, Prometheus's requests come from that container's network address, which isn't on the list, so every scrape is refused and Prometheus shows the target as down. You would set `METRICS_ALLOWED_ADDRESSES` to Prometheus's address (or its subnet).

### Exercise 35.4 ★★ Which metric?

`sdv_render_rejected_total` counts uploads refused with `503` because every render slot stayed busy. The governing settings are `max-concurrent-renders` (default 2) and `render-queue-timeout-seconds` (default 30): uploads wait up to that long for a slot before being refused. To reduce it, raise `max-concurrent-renders` (mindful of memory; the app container has a memory limit) or give more time, and check `sdv_render_seconds` and `sdv_render_timed_out_total` to see whether a few very slow PDFs are holding the slots.

### Exercise 35.5 ★★★ Design an alert

A model answer. Condition: `increase(sdv_sign_in_total{outcome="locked"}[15m]) > 5`, holding for 5 minutes (the throttle window is 15 minutes). Meaning: a throttle rule is firing repeatedly, so either guessing is happening or real users are locked out. First three steps: (1) open the admin audit log and filter for the lockout event, reading the `rule=` value (account and IP, IP, or account-wide) and the addresses; (2) if one address dominates, treat it as guessing and consider blocking it at the network edge; if the account-wide rule fired, tell the account's owner that a new device won't be accepted until the window passes or an administrator unlocks the account, and decide whether to press Unlock; (3) check whether `sdv_sign_in_total{outcome="success"}` is normal, which suggests the attack is failing. Any threshold with a stated window, a hold time, and a concrete first step earns credit.

### Exercise 35.6 ★★★ Explain the zero

A Prometheus series exists only after the app first exports it. If the counter for `outcome="locked"` were created lazily (only at the first lockout), then before that first lockout the series wouldn't exist. An alert rule such as `increase(sdv_sign_in_total{outcome="locked"}[15m]) > 5` compares against a series that isn't there, so it evaluates to nothing rather than to 0 or to true; and the very first lockout would appear as a new series with no earlier point to compute an increase from, so the rise from 0 to 1 can be missed. Registering every outcome up front exports each as 0 from the start, so the series is continuous and the rule always has a value to compare.
