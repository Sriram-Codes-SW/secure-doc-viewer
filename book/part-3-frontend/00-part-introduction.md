<!-- chapter: part III opener | part: III | owner: writer-frontend | tag: book-m6-final | status: draft -->
# Part III: The frontend

Part II built the server: it stores documents, checks who you are, and hands out watermarked tiles. But a server has no face. Everything a reader actually sees, from the sign-in form to the page they are reading, is drawn in the browser by a program that the server sends along. This part is about that program.

## What this part covers

The frontend in this repository is an **Angular** application written in **TypeScript**. Six chapters take you from the language to the finished screen:

- **Chapter 19: TypeScript.** The language, taught through the app's own data shapes and its asynchronous tile-fetching code.
- **Chapter 20: Node, npm and the Angular toolchain.** The tools that compile, run, and package the code, and the development proxy that keeps the browser and the API on one origin.
- **Chapter 21: Angular components and templates.** How a screen is built from components, and how signals keep it up to date. This includes the theme tokens and dark mode in `styles.css`.
- **Chapter 22: Talking to the backend.** Services, `HttpClient`, the interceptor that reacts to expired sessions, and the viewer's handling of rate limits and replaced documents.
- **Chapter 23: Routing, guards and forms.** Pages, route guards, sign-in and upload forms, deep links, and keyboard navigation.
- **Chapter 24: Testing the frontend.** Unit tests with Vitest, end-to-end tests with Playwright, and automated accessibility checks.

## Why this order

Each chapter needs only the ones before it. You need the language before the framework, and the toolchain before you can run anything. Components come before services because a service is only useful once something displays its data. Testing comes last because you can only test what you can read.

## A note on the code

Almost everything quoted here comes from `frontend/` at the tag `book-m6-final`. Where a chapter shows how something first looked, it names the earlier tag. The frontend folder first appears at `book-m1-accounts`; the earlier milestone, `book-m0-mvp`, used a single static page served by the backend, which Chapter 25 describes.

> **Note:** Tool versions differ slightly between tags. Vitest 5 and jsdom 30 appear only at `book-m6-final`; tags `book-m1-accounts` to `book-m5-platform` use Vitest 4.0.8 and jsdom 28. Angular 22 and TypeScript 6.0 are used throughout.

## What you will have at the end

You will be able to open any file under `frontend/src/app/`, say what it does and why it is written that way, and change it safely. Above all you will understand the viewer component, the capstone of the part: how it fetches tiles, waits out a rate limit with a countdown, zooms, turns pages by keyboard and swipe, and warns before your session ends.
