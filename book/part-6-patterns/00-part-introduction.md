<!-- chapter: 0 | part: VI | owner: writer-backend | tag: none | status: expanded -->
# Part VI: The design vocabulary

Parts I to V taught you to build, and to defend, one application. This part steps back and gives you the words that experienced engineers use when they talk about designs like it. Those words are the names of **patterns**: proven shapes of solutions to problems that recur. Knowing the names doesn't make you a better designer by itself. What it gives you is the ability to read a design quickly, to describe one in a sentence, and to explain a decision to someone else, including the cost of the alternatives.

Nothing in this part is new code. Every example is a file you have already met, and every claim about where a pattern lives was checked in the repository at a named tag. Where the project follows a pattern closely, the chapter says so; where it only approximates one, the chapter says that too. And because a beginner who has recently learned a name is tempted to use it everywhere, each pattern comes with the situations in which it would be clutter.

## What the part covers

Table VI.1 lists the two chapters and what each gives you.

**Table VI.1 — The chapters of Part VI**

| Chapter | Topic | What you gain |
|---|---|---|
| 38 | Design patterns in the code | The names for the small-scale solutions: strategy, chain of responsibility, template method, observer, state machine, bulkhead and more, on the backend and in Angular |
| 39 | Architectural patterns | The names for the large-scale shape: the single-page app with an API, layers, the modular monolith, the reverse proxy and trust boundary, sessions and signed URLs, the audit log as an event log, and a method for using patterns in decisions |

## How the two chapters relate

Chapter 38 works at the level of classes and methods: how one piece of code is arranged. Chapter 39 works at the level of the whole system: how the browser, the proxy, the application and the database are arranged. The same idea often appears at both levels. A chain of responsibility is a filter chain inside one server (Chapter 38) and a pipeline of proxies and checks across a request's whole route (Chapter 39). Chapter 37, which you may have read already, is the bridge: it weighs the project's big decisions against their alternatives, and the two chapters here give those alternatives their standard names.

## How to read this part

Read Chapter 38 after Part II and enough of Part III to recognize the Angular examples; read Chapter 39 after Part V, since it leans on deployment and operations. Each chapter follows the usual three tiers. The pattern descriptions share a fixed shape (the problem, the pattern, where it lives, what it costs, when not to use it), so that you learn to ask the same five questions of any pattern you meet in the future. The last of those questions, when *not* to use it, is the one that matters most. A design with a hundred patterns and no reason for any of them is worse than a plain one.

## What you will have at the end

You will be able to look at a class or a system diagram and say which patterns are in it, which are only approximated, and which are missing on purpose. You will also have a short method for making a design decision: start from the problem and its constraints, list the options, name the pattern behind each, state its cost, and write the decision down. The exercises in each chapter practice exactly that.
