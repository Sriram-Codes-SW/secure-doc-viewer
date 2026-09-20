# Epilogue: where to go from here

You started this book without any programming background. You now have a working web application,
built in stages, that you can explain from the browser to the database and back, and you have seen
it examined by independent AI reviewers and changed in response to what they found. That is not the same
as an application that cannot be attacked. Nothing is, and this epilogue is honest about where the
line falls. The application matters less than what you practiced while building it, so it is worth
stepping back to see how the parts fit together.

## How the seven parts fit together

**Part I gave you a footing.** You learned to work in a terminal and to read a file path, to write
and run Java, to build with Maven, to keep history with Git, to reason about requests, responses and
cookies, to ask a database questions in SQL, and to run programs in containers. None of that was
specific to this app, and all of it is what the rest of the book stands on.

**Part II built the server.** With Spring Boot you turned methods into web endpoints, checked every
input, stored data with JPA and evolved the schema with Flyway migrations, and signed people in with
Spring Security. You met the small, unglamorous defenses that make a server trustworthy: CSRF
tokens, security headers, atomic counting for a sign-in throttle, and bounded work for anything a
stranger can trigger. You learned to render PDFs, slice images, and sign URLs with an HMAC. Then you
tested all of it, including the concurrent cases that only fail one run in fifty.

**Part III built the browser side.** TypeScript gave you a type system for the messages between the
two halves. Angular gave you components, services, routes, and guards. You saw that a guard is a
courtesy to the user and never a security control, that the interceptor is where a `401` from any call
is handled in one place, and that end-to-end tests with an accessibility scanner catch what
unit tests can't.

**Part IV put it all together.** Seven milestones took the design from a demo that signed anyone in
by name to an application with accounts, ownership, sharing, an audit trail, upload, and API
hardening, a good reading experience, and a container platform with continuous integration. At each
step you saw the architecture blueprint change and read the decisions and challenges that caused the
change. The recurring lesson is that the design that survived review was rarely the first one:
several of the early protections, such as the sign-in throttle, the tile links, and the trusted client
address, had a gap that an AI reviewer found by trying to get around them, and each was fixed and tested.
That shows why review matters. It does not show that every gap was found.

**Part V took the app toward production.** A security review is a method, not a mood: name your
assets, your actors, and their entry points, then attack your own work. Deployment brought HTTPS, a
trusted proxy and a go-live checklist. Backups only count once you have practiced a restore. Metrics
and health checks tell you what the system is doing when you aren't looking, and supply-chain
controls reduce the chance that the things you didn't write become the way in. These chapters
describe how the app can be run and what to watch. They are not a guarantee that the running system
is safe.

**The trade-offs chapter closed the loop.** Every decision in the app has a price. Choosing to
render tiles on the server costs CPU and caching. Keeping sessions in memory costs you scale-out.
Building authentication yourself costs you the features an identity provider gives away. Naming
those costs, and the point at which you would choose differently, is what separates an engineer
who follows a design from one who owns it.

**Part VI gave the vocabulary.** Design patterns (Chapter 38) and architectural patterns (Chapter 39) put
names on what you had already seen: filter chains and strategies, state machines and bulkheads, layers,
gateways, event logs, and atomic switches. Naming a solution is what lets you recognize it in another
project and borrow it deliberately, and the five-step method in Chapter 39 tells you when not to.

**Part VII pointed past the single server, on paper.** Chapters 40 and 41 mapped the scale-out plan onto
AWS: containers behind a load balancer, a managed database, tiles in object storage, shared sessions
and counters, secrets from a managed store, and the operations around them. It is a design study. The
project never built or ran it, and the chapters say so. It showed what would carry over unchanged
(the watermark, the access re-check on every tile, the database pointer that acts as the atomic
switch), what would have to change, and when to stay put. Knowing when not to move is part of the same
skill.

## What you can now do

- Read Java, TypeScript, SQL, YAML, and Dockerfiles well enough to find where a behavior lives.
- Build a web service that stores data, checks its input, and answers errors without leaking internals.
- Build a browser client that talks to it, handles failure gracefully, and takes accessibility seriously.
- Test at three levels: small units, the running application, and a real browser.
- Package the system in containers, put it behind HTTPS, and describe how to back it up, restore it, and watch it.
- Explain, for any protection in the app, what attack it stops and what it does not.

## What the app does and does not defend

The application was reviewed in several rounds, and each finding that was demonstrated was fixed and
tested. That makes it defended against the specific attacks the reviews found and the classes they
represent: guessing sign-ins, replaying or sharing tile links, reading documents you were not shared
with, forging the client address, and flooding the server with expensive work. It does not, and cannot,
do the following, and the book says each of these where it matters:

- **It does not stop a screenshot or a photograph.** Anything shown on a screen can be copied. The
  watermark makes a leak traceable; it does not make copying impossible.
- **It has no second sign-in factor,** for administrators or anyone else, and sign-in is by password only.
- **A reader with a valid session can still fetch every page slowly.** The rate limit makes bulk
  harvesting slow, not impossible.
- **The tiles on disk are neither watermarked nor encrypted,** and they appear in backups, so whoever
  can read the storage can read the pages.
- **Sessions and counters live in memory,** so the application runs as one instance.
- **The pages are images,** so they have no text layer for screen readers.
- **Publishers can discover the names of other users** through the share picker, by design.
- **The cloud design of Part VII was never deployed,** so none of its claims has been tested on a real account.

If you deploy something like this for real people, treat the reviews in this book as a start, not an
end: have your own reviewers try to break it.

## Habits worth keeping

- **Ask what an attacker controls.** Every input, header, timing, and retry is a question.
- **Fail loudly at startup, quietly at runtime.** The app refuses to start with a weak signing
  secret, and it never puts internals in an error message.
- **Write the trade-off down.** A decision without its cost is a guess.
- **Test the unhappy path and the concurrent path.** Most real bugs live there.
- **Invite a fresh pair of eyes.** The project improved most when independent reviewers tried to
  break it. Do the same to your own work.
- **Be honest about limits.** The app raises the cost of copying and makes leaks traceable. It does
  not make copying impossible, and it says so.

## Where to go next

The repository's own list of possible next steps is a good set of projects. Each one changes the
architecture, so draw the next blueprint before you start (Appendix B shows how the earlier ones
evolved):

- Build the AWS design of Chapters 40 and 41, starting with its smallest useful first step. Sections 37.4 and
  37.7 describe what would change for tile delivery and storage, and what would stay.
- Share sessions and rate-limit counters across several app instances with a shared store.
- Add group-based sharing, expiry dates on access, or per-document sensitivity levels with tighter
  rate limits.
- Add multi-factor authentication or sign-in through an identity provider.
- Load only the tiles inside the viewport at the current zoom.

If you want to go deeper on a layer, pick the one that interested you most and read its official
documentation from the Further reading list of the matching chapter: the Java and Spring
documentation for the backend, the Angular documentation for the frontend, the MySQL reference for
data, and the OWASP guidance for security. Read the source of a library you depend on when
something surprises you; it is almost always more approachable than you expect.

Finally, build something of your own. The fastest way to keep what you learned is to use it on a
problem you care about, and to write down what went wrong and why. You now have the vocabulary,
the habits and a worked example to start from.
