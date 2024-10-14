# 6. Domain Plugins

Date: 2022-10-24

## Decision

A plugin paradigm (plugin-based architecture) will be used for the different business domains of the trusted intermediary.  This improves
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

## Impact

### Positive

Encapsulation of domain-specific logic: Ensuring isolation of concerns; Core application logic remains unaffected
Reusability: Plugins can be reused reducing duplication
Flexibility: Domains can evolve independently and potentially be extracted into separate projects.
Targeted testing: Focused unit and integration testing


### Negative 

- Increased complexity: Managing multiple plugins can increase architectural complexity over time, especially with the addition of features.
- Dependency management challenges: Ensuring compatibility across multiple version can become cumbersome


### Risks
- Maintenance burden: Possible increased maintenance burden which can require more rigorous testing
- Fragmentation: Increased possibility for inconsistencies across the system due to varying approaches

## Related Issues

- #13
