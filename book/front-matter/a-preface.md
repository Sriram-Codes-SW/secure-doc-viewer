# Preface

This book teaches you to build a real application from your first line of Java. You need no
programming experience. You need patience, a computer you can install software on, and curiosity
about how things work.

## What you will build

The **Secure Document Viewer** is a web application that shows PDF documents to signed-in people
without ever handing out the PDF. The server turns each page into small image pieces (tiles),
draws the viewer's identity on every piece, and delivers them through short-lived signed links.
This is the pattern commercial e-magazine readers use. By the last chapter you will have built it
and will understand every decision behind it.

## Why this project

A real project forces the questions tutorials skip. What happens when two people replace the same
file at once? Why does a sign-in form need a throttle? Why does a database need numbered
migrations? Each chapter teaches a tool because the app needs it, at the moment it needs it.

## What the app does not do

The app raises the cost of copying a document and makes leaks traceable. It cannot make content
uncopyable, because anything shown on a screen can be photographed. The book keeps that honesty
throughout, and Chapter 37 weighs every major decision against the alternatives.

## The stack

Java 25, Spring Boot 4, MySQL 8.4, TypeScript, Angular 22, Node 24, Docker and Git. You learn
only what this app uses.

## Acknowledgments

<!-- To be written by the editor at final assembly. -->
