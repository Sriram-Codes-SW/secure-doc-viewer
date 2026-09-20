# Preface

This book teaches you to build a real application from your first line of Java. You need no
programming experience. You need patience, a computer you can install software on, and curiosity
about how things work.

Most programming books teach a language and then leave you to work out what to do with it. This one
works the other way around. It starts from one concrete program, the **Secure Document Viewer**,
and teaches exactly the tools that program needs, at the moment it needs them. When you finish, you
will have built the application in the accompanying repository and you will understand why each
decision in it was made, including the ones that were made after something went wrong.

## What you will build

The Secure Document Viewer is a web application that shows PDF documents to signed-in people
without ever handing out the PDF. The server turns each page into small image pieces called tiles,
draws the viewer's identity on every piece, and delivers the pieces through short-lived signed
links. It has accounts and roles, private and shared documents, an audit trail, a reader that works
on a phone, and a set of protections against the ways people try to abuse it. It runs in Docker,
sits behind an HTTPS frontend, and can be backed up and watched.

This is the pattern commercial e-magazine and flipbook readers use. The book explains it from the
ground up, so that "signed URL," "session binding" and "rate limit" are ideas you can use on your
next project and not only words in this one.

## Why a real project

A real project forces the questions a tutorial can skip. What happens when two people replace the
same file at the same moment? Why does a sign-in form need a throttle, and why must counting the
attempts be one indivisible step? Why does a database need numbered migrations? Why does the
server answer "not found" when the honest answer is "not allowed"? In a tutorial these questions
never come up. In a real application they come up in the first week of use, and the answers are
where most of the learning is.

The project was built in stages, and the repository keeps a bookmark (a Git tag) at the end of
each stage. Part IV walks through those stages in order, so you watch the design grow: from a
one-page demo that signs anyone in with only a name, to an application that has survived several
rounds of independent review. Along the way you meet the bugs and the review findings that shaped
it. They are told as stories: the problem, how it was found, the fix, and the lesson.

## How the book is organized

- **Front matter** gets your machine ready.
- **Part I, Foundations,** teaches programming with Java, the command line, Maven, Git, how the
  web works, SQL, and Docker. No framework yet.
- **Part II, The backend,** builds the server side with Spring Boot: web endpoints, storing data,
  signing people in, defending against abuse, working with PDFs, and images, and testing.
- **Part III, The frontend,** builds the browser side with TypeScript and Angular, and shows how it
  talks to the backend and how it is tested.
- **Part IV, Building the Secure Document Viewer,** assembles the whole application in seven
  milestones, each ending with an updated architecture diagram and the decisions and challenges of
  that stage.
- **Part V, Production,** takes the finished app to a real host: security review, deployment and
  HTTPS, backups, metrics, and supply-chain hygiene.
- **Engineering trade-offs (Chapter 37)** weighs every major decision against the alternatives,
  including what an enterprise would do differently.
- **Part VI, Patterns,** names the design and architectural patterns the app
  already uses, so you leave with a vocabulary for your next project.
- **Part VII, Taking it to the cloud,** closes the main text with a design for running the app on Amazon
  Web Services. It is a design, not a deployment, and it says so.
- The **appendices** hold the glossary, the architecture blueprints side by side, solutions to the
  exercises, command cheat sheets, and troubleshooting, followed by an index of the terms defined in
  the chapters.

## What the app does not do

The app raises the cost of copying a document and makes leaks traceable. It cannot make content
uncopyable, because anything shown on a screen can be photographed. The book keeps that honesty
throughout. Being clear about what a design does not achieve is part of engineering it, and
Chapter 37 puts every major decision next to its costs.

## The stack

Java 25, Spring Boot 4, MySQL 8.4, TypeScript, Angular 22, Node 24, Docker, and Git. You learn
only what this app uses. Topics the app doesn't need are left out or mentioned in a sentence.
Where an earlier milestone used older versions (the first five milestones were built with Spring
Boot 3.3.4 and Java 21), the chapter says so.

## A note on how this project was made

The application was built with an AI coding assistant working alongside the project owner, and it was
examined between milestones by two independent AI review agents: the AI product-owner reviewer and the
AI technical-manager reviewer. The book later calls them the PO reviewer and the TM reviewer. It says
so wherever it matters and never presents those reviewers as people. What matters for you is that the findings were real: each was demonstrated
against the running application, and each led to a change you can read in the repository.

## A promise about honesty

Everything in this book that is presented as the project's code is copied from the repository at a
named tag. Where a listing is shortened, its caption says so. Where the book simplifies an idea for
a beginner, it says so and tells you which chapter gives the full picture. If you find a place
where it doesn't, that is a defect in the book, not a gap in your understanding.

## Acknowledgments

This book stands on the work of the people who build and maintain open-source software and the
documentation that comes with it. The Secure Document Viewer is made of their projects, and every
chapter sends you back to their official documentation: OpenJDK and Java, Spring Boot, Spring
Security and Spring Session, Hibernate, and Flyway, Apache Maven, Apache PDFBox, and Apache Tomcat,
MySQL, Docker, Git, Node.js, npm, TypeScript, and Angular, Vitest, Playwright, and axe-core,
Testcontainers, nginx, and Caddy, and, for the diagrams, Mermaid. Thank you to their maintainers and
to everyone who writes and corrects their documentation.

The author of this book is Claude, an AI model made by Anthropic. The application was built, and the
book was written, together with the project owner, who set the goals, made the product decisions and
reviewed the work. The PO reviewer and the TM reviewer, both AI agents,
examined the application between milestones, and further AI agents helped research, write, review
and check the chapters. The author checked the claims in the book against the code and the official
documentation, and the book says so wherever the difference between checked and unchecked matters.
Readers who find an error are the last and most important reviewers, and their corrections are
welcome.
