# 5. Option-enabling Software Architecture (OeSA)

Date: 2022-10-24

## Decision

We will use use OeSA to inform how we add new features and refactor.

## Status

Accepted.

## Context

OeSA is similar to [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html).
There are a couple main concepts that we strive to maintain while also being pragmatic.

1. Less important code depends on more important code.  For example, third-party dependencies such as ReST
   frameworks, and database ORMs.  This allows for swapping out less important code without affecting the more
   important code.
2. Dependency inversion.  This allows the path of code execution not to follow the dependency path of
   less important code depending on more important code.  For example, when business logic needs to call the
   database.

### Related Issues

- #1
- #13
