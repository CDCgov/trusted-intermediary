# 4. Groovy and Spock for Testing

Date: 2022-10-24

## Decision

We will use the [Groovy programming language](http://groovy-lang.org) with the
[Spock testing framework](https://spockframework.org) for our unit and end-to-end testing.

## Status

Accepted.

## Context

Spock is a testing framework similar to [JUnit](https://junit.org/junit5/).  Some benefits of Spock include...
- Encourages BDD-style given/when/then testing.
- Mocking is _super_ easy.  No need for a separate dependency like [Mockito](https://site.mockito.org).
- Expressive assertion failures.  No need for a separate dependency like [Google Truth](https://truth.dev).
- Adds a layer of flexibility and expressiveness to testing that you don't get with JUnit.

Spock requires we use Groovy, another JVM-based language.

## Impact

### Positive

- Expressive and readable tests
- Supports data-driven testing which is useful for testing FHIR resources across a variety of scenarios with different data sets
- Integrates well with Java

### Negative

- Limited IDE support

### Risks

- Tests can run slow
- Dependency management difficulties

## Related Issues

- #1
- #13
