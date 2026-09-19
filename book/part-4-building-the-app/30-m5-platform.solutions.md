<!-- chapter: 30 | part: IV | owner: writer-app | tag: see chapter | status: draft -->
# Solutions for Chapter 30

### Exercise 30.1 ★ Forwarded header

Any client can send the header. If the backend believed every sender, a client could pretend to be any address and reset per-address lockouts. Trust belongs to a network position (nginx's fixed address), not to the header.

### Exercise 30.2 ★★ Lockout abuse

Failing from several addresses tripped the account-wide count and blocked the real owner. The final rule applies the account-wide limit only to unrecognised devices; a device that signed in successfully within 30 days is recognised and keeps working. The cost: a correct password from a new device is refused until an admin unlocks the account.

### Exercise 30.3 ★★★ Version inside the token

Only signed fields are tamper-proof. A separate parameter could be edited to ask for an old or new render without invalidating the token. Signing it means a stale URL is refused (410) and can't be repurposed.

