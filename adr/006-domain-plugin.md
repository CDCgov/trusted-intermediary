# 6. Domain Plugins

Date: 2022-10-24

## Decision

A plugin paradigm (plugin-based architecture) will be used for the different business domains of the intermediary. This improves modularity, separates concerns, and allows for flexibility in the future if it makes sense to extract this domain into a separate JAR, Gradle project, or even repository.

## Status

Accepted.

## Context

The intermediary currently serves a single business domain, but it has the potential to support multiple domains in healthcare where a intermediary is beneficial. To accommodate this, we will adopt a plugin-based architecture that allows new domains to be registered with the intermediary. This approach enables each domain to be developed and operated independently of the intermediary’s core system.

This independence also applies to the intermediary’s deployment environment, whether the intermediary is running in a Jetty Java container on a VM, or hosted within Azure Functions, the plugin paradigm ensures that domain logic remains unaffected and adaptable to various operational contexts.

## Impact

### Positive

- **Encapsulation of domain-specific logic** ensuring isolation of concerns; Core application logic remains unaffected.


- **Reusability:** Plugins can be reused reducing duplication.


- **Flexibility:** Domains can evolve independently and potentially be extracted into separate projects.


- **Targeted Testing:** focused unit and integration testing


### Negative 

- **Increased Complexity:** Managing multiple plugins can increase architectural complexity over time, especially with the addition of features.

- **Dependency Management Challenges:** Ensuring compatibility across multiple versions can become cumbersome.


### Risks
- **Maintenance Burden:** possible increased maintenance burden which can require more rigorous testing

- **Fragmentation Risk:** increased possibility for inconsistencies across the system due to varying approaches

## Related Issues

- #13
