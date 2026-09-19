<!-- chapter: part-V | part: V | owner: writer-production | tag: book-m5-platform | status: draft -->
# Part V: Production

By the end of Part IV you have a working app. Part V asks a different question: is it ready for people you don't know, on a machine you don't sit in front of?

The chapters follow the order in which a real launch unfolds:

- **Chapter 32, Security review and threat modeling:** look at the app the way an attacker does, and read the review rounds that hardened it.
- **Chapter 33, Deployment and TLS:** put nginx and Caddy in front of the app, and work through the go-live checklist.
- **Chapter 34, Backups, restores and operations:** capture the database and the tiles consistently, and prove you can restore them.
- **Chapter 35, Health, metrics and alerting:** let the app report on itself, and decide what deserves an alert.
- **Chapter 36, Supply chain and CI:** check the code and images you didn't write, on every change.

Chapter 37, the trade-offs chapter, comes after this part. It weighs every major decision in the app, including the ones this part relies on, such as running a single instance.

Part V leans on the last milestone tags: most code and configuration is quoted at `book-m5-platform`, where the Docker stack, CI, and hardening arrived, and Chapter 36 also touches `book-m6-final`, where the Dependabot rules and the final dependency updates landed.

When you finish, you'll be able to read the compose file, the nginx and Caddy configuration, the CI workflow, and the README's go-live checklist, and explain the reason for each line.
