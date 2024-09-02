# 3. Gradle

Date: 2022-10-24

## Decision

We will use the [Gradle build tool](https://gradle.org) for the main trusted intermediary code.

## Status

Accepted.

## Context

[Maven](https://maven.apache.org) is the other big option available to us.  Gradle was chosen over Maven because
- it uses Groovy, an actual programming language, to configure the projects instead of XML, which has a rigidly defined schema.
  This allows for better expressibility in how one configures their project.
- Gradle is faster with its job execution.


## Impact

### Positive

- Advanced dependency management capabilities help streamline the integration of FHIR libraries, such as HAPI FHIR, and other required dependencies.
- Highly customizable build scripts allow for fine-tuned configuration
- Reduced build times
- Integrates well with continuous integration and continuous deployment (CI/CD) pipelines, enabling automated testing

### Negative

- Flexibility can lead to complex and verbose build scripts; Managing these scripts can become cumbersome
- Custom configurations may not be fully compatible with certain versions of Java or FHIR libraries

### Risks

- Possible slow build times due to scripts and project size

## Related Issues

- #1
- #13
