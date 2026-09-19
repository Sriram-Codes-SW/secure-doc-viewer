# Solutions: Chapter 36

### Exercise 36.1 ★ Which job?

A failing unit test: Backend tests (or Frontend tests for TypeScript). A vulnerable Maven library: the OSV job. An outdated OS package in the nginx image: the Trivy step in the end-to-end job, which scans the built `secure-doc-viewer-web` image. A broken sign-in screen: the end-to-end job (Playwright drives a real browser against the built stack).

### Exercise 36.2 ★★ Why pin?

Git tags can be moved by whoever controls the action's repository. If `actions/checkout@v7` were moved to malicious code, your next CI run would execute it with your workflow's permissions and could read secrets or tamper with build output. A full commit SHA identifies one immutable commit, so the code that runs is the code you reviewed. The project also restricts the workflow with `permissions: contents: read` to limit the damage of any single compromised step.

### Exercise 36.3 ★★★ Write a rule

Today the `typescript` rule ignores minor and major updates, so Dependabot would not propose 6.1 at all (Angular 22 accepts only `>=6.0 <6.1`). At the Angular 23 upgrade a person would upgrade Angular first (or together with TypeScript) by hand, follow Angular's supported version range, and then update the ignore rule so it matches the new range, for example allowing patches of the new minor. The rule exists so that TypeScript moves in step with Angular, never ahead of it.
