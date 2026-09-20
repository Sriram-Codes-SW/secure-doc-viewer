<!-- chapter: 0 | part: VII | owner: writer-production | tag: none | status: expanded -->
# Part VII: Taking it to the cloud

Parts I to VI took the Secure Document Viewer from a first line of Java to a production-ready single server. They also gave you the vocabulary to explain it. Part VII asks what comes next when one server is no longer enough, or when one server's risks are no longer acceptable: what would it take to run the same app on a cloud platform, and would that make the app's actual services better?

Better is not automatic. A cloud design adds availability and recoverability, and it adds new hard dependencies (a shared cache, object storage, a load balancer), new permissions to get wrong, and new bills. The two chapters weigh both sides. This part is a design study, not a step-by-step build guide: you can read it, and do its exercises, without an Amazon Web Services (AWS) account.

## What the part covers

Table VII.1 lists the two chapters and the question each one answers.

**Table VII.1 — The chapters of Part VII**

| Chapter | Topic | The question it answers |
|---|---|---|
| 40 | Designing a move to AWS: compute, network, data, and state | Which AWS building block replaces each piece of the Compose stack, what would change in the code, and what has to stay atomic when state is shared? |
| 41 | Running it on AWS: secrets, operations, edge, and cost | Who may do what, how do you see, recover, and ship it, what do the optional edge services add, what does it cost, and when should you not go? |

## An honest frame

This part is a **design**. The project never built or ran the Secure Document Viewer on AWS, and each chapter says so up front and again wherever it matters. What the chapters offer instead is a design grounded in two things you can check. Everything they say about the app comes from the code at `book-m6-final`, with the class or setting named. Everything they say about an AWS service was checked against the official AWS documentation on the date given in the chapter, and each such statement is tagged with a number, such as (source 4), that points into a numbered list of documentation pages at the end of the chapter. "Checked" means read and compared, not tried. Code marked "illustrative" was not applied to any account, and the chapters deliberately give no prices and no benchmarks: use the AWS Pricing Calculator with your own numbers.

## Before you start

Part VII builds on Chapters 32 to 37 and on Chapter 39, and it says so in each chapter's prerequisites:

- Chapters 32 to 36 give the security review, deployment, backups, metrics, and supply chain that the design re-plans.
- Chapter 37, especially Section 37.17, is the seven-step plan that Chapter 40 maps onto AWS services.
- Chapter 39's patterns (immutable versions, twelve-factor configuration, defense in depth) explain why some parts of the app survive the move unchanged.

You need no AWS knowledge. Every AWS term is defined where it first matters, next to the Compose piece it replaces.

## How to read this part

- **Read Chapter 40 before Chapter 41.** The second chapter operates the design of the first.
- **Keep the Compose stack in mind.** Most AWS services in Chapter 40 are introduced next to the Compose piece they replace; a few, such as the shared cache, are new because the single-server app never needed them.
- **Ask "do I need this at all?" first.** Section 40.4 and Table 41.2 say when staying on one server is the right answer.
- **Notice what the cloud does not fix.** The screenshot problem, the missing multi-factor authentication (MFA), and the slow-but-possible harvest are limits of the design, not of the hosting.
- **Take the smallest useful step.** Chapter 41 closes with a migration order that starts with moves that help even a single copy.

## What you will have

When you finish, you'll be able to take a cloud architecture diagram and say, for each box, which part of the Secure Document Viewer it replaces, what would change in the code, and what new risk it introduces. You'll also be able to say when the honest answer is to leave the app where it is.
