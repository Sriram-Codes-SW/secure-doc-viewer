# Epilogue: where to go from here

You started this book without any programming background. You now have a working, hardened web
application that you can explain from the browser to the database and back. The application matters
less than what you practiced while building it, so it is worth stepping back to see how the parts
fit together.

## How the five parts fit together

**Part I gave you a footing.** You learned to work in a terminal and to read a file path, to write
and run Java, to build with Maven, to keep history with Git, to reason about requests, responses and
cookies, to ask a database questions in SQL, and to run programs in containers. None of that was
specific to this app, and all of it is what the rest of the book stands on.

**Part II built the server.** With Spring Boot you turned methods into web endpoints, checked every
input, stored data with JPA and evolved the schema with Flyway migrations, and signed people in with
Spring Security. You met the small, unglamorous defenses that make a server trustworthy: CSRF
tokens, security headers, atomic counting for a sign-in throttle, and bounded work for anything a
stranger can trigger. You learned to render PDFs, slice images and sign URLs with an HMAC. Then you
tested all of it, including the concurrent cases that only fail one run in fifty.

**Part III built the browser side.** TypeScript gave you a type system for the messages between the
two halves. Angular gave you components, services, routes and guards. You saw that a guard is a
courtesy to the user and never a security control, that the interceptor is where cookies, tokens and
errors are handled in one place, and that end-to-end tests with an accessibility scanner catch what
unit tests can't.

**Part IV put it all together.** Seven milestones took the design from a demo that signed anyone in
by name to an application with accounts, ownership, sharing, an audit trail, hardening, a good
reading experience and a production platform. At each step you saw the architecture blueprint
change and read the decisions and challenges that caused the change. The recurring lesson is that
the design that survived review was rarely the first one: the first version of nearly every
protection had a gap that someone found by trying to get around it.

**Part V made it real.** A security review is a method, not a mood: name your assets, your actors
and their entry points, then attack your own work. Deployment brought HTTPS, a trusted proxy and a
go-live checklist. Backups only count once you have practiced a restore. Metrics and health checks
tell you what the system is doing when you aren't looking, and supply-chain controls keep the
things you didn't write from becoming the way in.

**The trade-offs chapter closed the loop.** Every decision in the app has a price. Choosing to
render tiles on the server costs CPU and caching. Keeping sessions in memory costs you scale-out.
Building authentication yourself costs you the features an identity provider gives away. Naming
those costs, and the point at which you would choose differently, is what separates an engineer
who follows a design from one who owns it.

## What you can now do

- Read Java, TypeScript, SQL, YAML and Dockerfiles well enough to find where a behavior lives.
- Build a web service that stores data, protects it and answers errors without leaking internals.
- Build a browser client that talks to it, handles failure gracefully and stays accessible.
- Test at three levels: small units, the running application, and a real browser.
- Package the system, deploy it behind HTTPS, back it up, restore it and watch it.
- Explain, for any protection in the app, what attack it stops and what it does not.

## Habits worth keeping

- **Ask what an attacker controls.** Every input, header, timing and retry is a question.
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

- Store tiles in object storage and serve them with cloud-signed URLs (Sections 37.4 and 37.7
  describe what would change and what would stay).
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
