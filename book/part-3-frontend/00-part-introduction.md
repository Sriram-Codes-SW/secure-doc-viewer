<!-- chapter: part III opener | part: III | owner: writer-frontend | tag: book-m6-final | status: expanded -->
# Part III: The frontend

Part II built the server: it stores documents, checks who you are, and hands out watermarked tiles. But a server has no face. Everything a reader actually sees, from the sign-in form to the page they are reading, is drawn in the browser by a program that the server sends along. This part is about that program.

## Where the frontend fits

Chapter 1 introduced the client and the server: the **client** is the program on the reader's device that asks for things, and the **server** is the program that answers. In this project the client is a web application that runs inside the reader's browser, and it is called the **frontend**. The server is the Spring Boot **backend** from Part II. The two never share code. They share only an agreement: a set of web addresses and the shape of the JSON they exchange (Chapters 8 and 12).

That agreement is why the frontend is a separate program with its own language, its own tools, and its own tests, in its own folder, `frontend/`. It is also why some of the most interesting questions in the whole app are asked at the seam between the two. Who checks that a reader may open a document: the button that hides it, or the server that refuses to serve it? (The server; the button is a courtesy.) Where does the session live: in JavaScript the reader could inspect, or in a cookie the browser guards? (A cookie the script can't read.) How does the page know to slow down when the server says "too many requests"? Part III answers these from the browser's side, and Part II showed the other side.

## What this part covers

The frontend is an Angular application written in TypeScript. Six chapters take you from the language to the finished screen:

- **Chapter 19: TypeScript.** The language, taught through the app's own data shapes, a worked reading of the tile-layout function, and its asynchronous tile-fetching code.
- **Chapter 20: Node, npm, and the Angular toolchain.** The tools that compile, run, and package the code, a trace from typing `npm start` to seeing the app, and the development proxy that keeps the browser and the API on one origin.
- **Chapter 21: Angular components and templates.** How a screen is built from components, how signals keep it up to date, and how the theme tokens in `styles.css` handle dark mode and contrast. It ends with how the viewer paints a page from tiles.
- **Chapter 22: Talking to the backend.** Services, `HttpClient`, the interceptor that reacts to expired sessions, the search-as-you-type pipeline, and the viewer's handling of rate limits and replaced documents.
- **Chapter 23: Routing, guards, and forms.** Pages, route guards, sign-in and upload forms, a safe handling of the saved return address, deep links, keyboard navigation, and swiping.
- **Chapter 24: Testing the frontend.** Unit tests with Vitest, end-to-end tests with Playwright, and automated accessibility checks.

## Why this order

Each chapter needs only the ones before it. You need the language before the framework, and the toolchain before you can run anything. Components come before services because a service is only useful once something displays its data. Routing and forms come after HTTP because most screens are reached through a route and finish by sending a request. Testing comes last because you can only test what you can read.

Part III also follows the same three-level structure as the rest of the book (the front matter explains how to read the tiers). The **beginner tier** of each chapter gives you enough to follow the milestone chapters in Part IV. The **intermediate tier** shows how the pieces talk to each other and why this tool was chosen over the obvious alternative. The **advanced tier** covers security, performance, and real incidents from the project. On a first read you may skip the second and third tiers of any chapter and come back later.

## The frontend at a glance

Table III.1 is the map you will fill in as you read. Every path is inside `frontend/`, and every file appears in a chapter.

**Table III.1 — The frontend at a glance**

| Path | What it is | Chapter |
|---|---|---|
| `package.json`, `package-lock.json`, `angular.json`, `tsconfig*.json`, `proxy.conf.json` | Tooling and configuration | 20 |
| `src/main.ts`, `src/index.html` | Where the app starts | 20 |
| `src/styles.css`, `src/app/app.*` | The shell: theme tokens, top bar, idle banner | 21 |
| `src/app/core/` | Session, interceptor, guards, idle timing | 22, 23 |
| `src/app/features/documents/`, `auth/`, `admin/` | The screens for lists, sign-in, and administration | 21–23 |
| `src/app/features/viewer/` | The page viewer, the capstone | 19, 21–23 |
| `nginx.conf`, `Dockerfile` | How the built app is served in production | 20, 22 |
| `*.spec.ts`, `e2e/`, `playwright.config.ts` | Tests | 24 |

## Getting ready

You need Node 24 and npm installed (the setup chapter in the front matter shows how) and a copy of the repository. You do not need a running backend to read Part III, but you will get more from it if you start one: an exercise in Chapter 22 asks you to look at real requests in your browser's developer tools. Each chapter names the tag its code comes from. The frontend folder first appears at `book-m1-accounts`; the earlier milestone, `book-m0-mvp`, used a single static page served by the backend, which Chapter 25 describes. Every chapter quotes code from `book-m6-final` unless it names another tag, and you can see any file at any tag with `git show <tag>:<path>`.

> **Note:** Tool versions differ slightly between tags. Vitest 5 and jsdom 30 appear only at `book-m6-final`; tags `book-m1-accounts` to `book-m5-platform` use Vitest 4.0.8 and jsdom 28. Angular 22 and TypeScript 6.0 are used throughout.

## A few ideas to carry through the part

- **The browser is not trusted.** Every check in the frontend, from a hidden button to a validated form, exists to help honest readers. Every real rule is enforced again on the server.
- **Say what you mean in types and names.** The frontend's data shapes, its state names (`pending`, `loaded`, `failed`) and its test names are written so that the code explains itself.
- **Small, testable decisions.** Where behavior depends on time or on the network, the project pulls the decision into a small function that can be tested without waiting.
- **One origin.** The page and the API live at the same address from the browser's point of view. Cookies, CSRF protection, and the content security policy all rest on that.

## What you will have at the end

You will be able to open any file under `frontend/src/app/`, say what it does and why it is written that way, and change it safely. Above all you will understand the viewer component, the capstone of the part: how it fetches tiles, waits out a rate limit with a countdown, zooms, turns pages by keyboard and swipe, and warns before your session ends. Part IV then puts the frontend and the backend together, one milestone at a time.
