# 6. Domain Plugins

Date: 2022-10-24

## Decision

A plugin paradigm will be used for the different business domains of the trusted intermediary.  This improves
modularity, separates concerns, and allows for flexibility in the future if it makes sense to extract this domain
into a separate JAR, Gradle project, or even repository.

## Status

Accepted.

## Context

The trusted intermediary useful for just one business domain.  There are multiple areas in healthcare where
a trusted intermediary could be beneficial.  To that end, a plugin paradigm is used to register domain(s) with the
larger trusted intermediary application.  This also allows the domain to develop and operate regardless how the
trusted intermediary operates.  E.g. Is the trusted intermediary in a Jetty Java container hosted on a VM or is it
hosted in Azure Functions?

### Related Issues

- #13
