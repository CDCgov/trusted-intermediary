# 3. Gradle

Date: 2022-10-24

## Decision

We will use the [Gradle build tool](https://gradle.org) for the main trusted intermediary code.

## Status

Accepted.

## Context

[Maven](https://maven.apache.org) is the other big option available to us.  Gradle was chosen over Maven because
- it uses Groovy, an actual programming language, to configure the projects instead of XML, which has a rigidly defined schema.
  This allows for better expressability in how one configures their project.
- Gradle is faster with its job execution.


### Related Issues

- #1
- #13
