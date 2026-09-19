# Epilogue: where to go from here

You started with no programming background and finished with a working, hardened web application
that you understand from the browser to the database. That is a real achievement, and the
important part isn't the app itself. It's the habits you practiced while building it.

## What you now know how to do

- Read and write Java, TypeScript, SQL and configuration files, and explain what each line does.
- Build a server that answers requests, stores data, and refuses the people who shouldn't see it.
- Test your work at three levels: small units, the whole application, and a real browser.
- Package the app in containers, put it behind a proxy with HTTPS, back it up, and watch it.
- Treat security as a chain of small, checkable decisions rather than a single feature.

## Habits worth keeping

- **Ask what an attacker controls.** Every input, header and timing is a question.
- **Fail loudly at startup, quietly at runtime.** The app refuses to start with a weak signing
  secret, and it never leaks internals in an error.
- **Write down the trade-off.** Chapter 37 exists because every good decision has a cost.
- **Review with fresh eyes.** The project improved most when independent reviewers tried to break
  it. Do the same to your own work.
- **Be honest about limits.** The app raises the cost of copying and makes leaks traceable. It
  doesn't make copying impossible, and it says so.

## Ideas for your next steps

The repository's own "Possible next steps" are a good list, and each is a project:

- Store tiles in object storage and serve them with cloud-signed URLs (Chapter 37.4 and 37.7).
- Share sessions across several app instances with a shared session store.
- Add group-based sharing, expiry dates on access, or per-document sensitivity levels with tighter
  rate limits.
- Add multi-factor authentication or sign-in through an identity provider.
- Load only the tiles inside the viewport at the current zoom.

Choose one, write down what you expect it to change in the architecture, and draw the next
blueprint (Appendix B shows how the earlier ones evolved).

## Keep learning

Read the official documentation for the tools you used; each chapter's Further reading lists
the starting points. Read the code of libraries you depend on when something surprises you. And
build something of your own: the fastest way to keep what you have learned is to use it on a
problem you care about.
