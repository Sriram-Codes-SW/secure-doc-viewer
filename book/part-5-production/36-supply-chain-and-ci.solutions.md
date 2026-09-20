# Solutions: Chapter 36

### Exercise 36.1 ★ Which job?

A failing unit test: Backend tests (or Frontend tests and build for a TypeScript test). A vulnerable Maven library: the OSV job. An outdated operating system package in the nginx image: the Trivy step in the end-to-end job, which scans the built `secure-doc-viewer-web` image. A broken sign-in screen: the end-to-end job, where Playwright drives a real browser against the built stack.

### Exercise 36.2 ★ Read the pin

The workflow uses the long hash (`3d3c42e5...`), the commit SHA. The `# v7.0.1` comment is only for humans, so they can see which release the hash corresponds to. It matters because a tag such as `v7` can be moved to different code by whoever controls the action's repository, while a commit SHA identifies one fixed commit; the pinned workflow runs the code that was reviewed.

### Exercise 36.3 ★★ Why pin?

If the tag were moved to malicious code, the next run would execute it inside your workflow, where it could read the checked-out source and any secrets that job has, or tamper with build output. The setting `permissions: contents: read` near the top of the workflow limits the damage: the workflow's token can read the repository but can't push to it. Pinning by SHA prevents the swap in the first place.

### Exercise 36.4 ★★ Read the flags

`--severity HIGH,CRITICAL` reports only serious findings; it hides medium and low ones, which may still matter in combination or in your context. `--ignore-unfixed` skips findings for which no fixed version exists; it hides serious flaws that you can't fix yet, so someone has to watch the advisories. `--exit-code 1` makes Trivy exit with an error when it reports any finding (after the other two flags have filtered), which is what turns the step red; without it, Trivy would print findings and the job would still pass, hiding them from anyone who doesn't read the log.

### Exercise 36.5 ★★★ Handle a finding

A model answer. (1) Read the advisory: the library, the vulnerable range, and the fixed version. (2) Confirm the fixed version is a patch release that should be compatible. (3) Override the managed version in `pom.xml`, in the same way the project overrode Tomcat: add a property such as `<library.version>1.2.4</library.version>` (the exact property name comes from the framework's dependency management for that library). (4) Write a comment above it: name the advisory identifiers, say which framework version ships the older library, and say when to remove the override ("Drop this once the framework manages 1.2.4 or newer"). (5) Run `./mvnw -B verify`, then let CI run the OSV job and the end-to-end job. (6) Add a calendar reminder or an issue to remove the override when the framework catches up. Any answer with an override, a comment naming the advisories and the removal condition, and a full test run earns credit.

### Exercise 36.6 ★★★ Write a rule

The exercise is hypothetical. The `typescript` rule ignores minor and major updates, so Dependabot would not propose the newer minor version at all; the project's Angular 22 accepts only `>=6.0 <6.1`. When a person upgrades Angular, they would upgrade Angular and TypeScript together by hand, following the supported range in the new Angular release's notes, and then adjust the ignore rule to match, for example allowing patches of the new minor. The rule exists so that TypeScript moves in step with Angular, never ahead of it.
