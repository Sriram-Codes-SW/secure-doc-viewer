<!-- chapter: part-V | part: V | owner: writer-production | tag: book-m5-platform, book-m6-final | status: expanded -->
# Part V: Production

By the end of Part IV you have a working app. It signs people in, shares documents, tiles and watermarks pages, and records who did what. Part V asks a different question: is it ready for people you don't know, on a machine you don't sit in front of, on a day when something goes wrong?

That question is not the same as "does it work?". A prototype works when everything goes right. A production system is judged by what happens when a stranger sends a forged header, when the disk fills up, when a library you didn't write turns out to have a critical flaw, or when the person who built it is asleep. Part V teaches the habits and the machinery for those days.

## What the part covers

| Chapter | Topic | The question it answers |
|---|---|---|
| 32 | Security review and threat modeling | How would someone attack this, and how do you find out before they do? |
| 33 | Deployment and TLS | How does the app reach a real server, and how do you keep the traffic private? |
| 34 | Backups, restores and operations | If the disk dies tonight, what do you get back tomorrow? |
| 35 | Health, metrics and alerting | How do you know what the running app is doing? |
| 36 | Supply chain and CI | How do you trust the code and images you didn't write? |

*Table 1 — The chapters of Part V*

After these five chapters comes Chapter 37, "The engineering trade-offs", which steps back and weighs every major decision in the app. It sits outside Part V on purpose: it draws on all five parts of the book, and you will get more from it once you have seen the whole system in production.

## Why this order

The order follows a real launch. You start by thinking like an attacker (Chapter 32), because every later decision is easier when you know what you are protecting the app from. You then build the front door (Chapter 33), with nginx in front of the app and Caddy for HTTPS. You plan for failure (Chapter 34) before you need to, because the day you need a backup is the worst day to invent one. You add eyes (Chapter 35), so you notice problems without waiting for a user to report them. Finally you look at the code and images you inherit (Chapter 36), and at the automation that checks them on every change.

Part V leans on the last two milestone tags. Most code and configuration is quoted at `book-m5-platform`, where the Docker stack, CI, and the hardening from five rounds of review arrived. Chapter 36 also touches `book-m6-final`, where Dependabot's update rules and the last dependency updates landed. Both tags share the same compose file, nginx configuration, and Caddyfile, so what you read matches what you can run.

## A note on the reviewers

Chapters 32 to 34 describe findings from two review roles, a "Product Owner reviewer" and a "Senior Technical Manager reviewer". These were AI review agents, briefed to act as independent third parties. A human product owner made the product decisions. The book tells you this each time it matters, because being honest about who found what is part of describing a real project.

## How to read this part

- **Read it in order the first time.** Chapter 33's fixed addresses only make sense after Chapter 32's forged-header story, and Chapter 34's janitor only makes sense after you have seen versioned tiles.
- **Try the commands.** Chapters 33, 34 and 35 are written so that you can run the stack, take a backup, and read the metrics page on your own machine. A restore you have practiced is worth more than one you have read about.
- **Notice the honesty.** These chapters keep the README's habit of stating what the app does not do. The threat model names what is not defended; the backup chapter admits that a backup stops the app; the trade-offs chapter labels which reasoning was recorded and which is general practice.
- **Use the tiers.** Each chapter's beginner tier gives you the idea and the vocabulary. The intermediate and advanced tiers carry the detail; if you are reading for the first time, you can skip to "In this project" and come back.

## If you already know some of this

If you have deployed software before, skim the beginner tiers, and slow down where the project's own history changes the textbook answer:

- Chapter 32's incident about a proxy that appended to a forged header shows how a "secure" pull request introduced a High finding.
- Chapter 34's janitor refuses to delete tiles when a document's current version is missing, and explains why.
- Chapter 36's Dependabot rules are not the defaults: they exist because three automatic proposals were wrong for this project.

## What you will have built

When you finish Part V you will not have written more code. You will have something that matters as much: the ability to take the app to a real host, keep it private and recoverable, watch it, keep its dependencies honest, and explain why every line of its configuration is there. That is the difference between an app that runs and an app you can run for other people.
