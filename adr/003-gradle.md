# 3. Gradle

Date: 2022-10-24

## Decision

We will use the [Gradle build tool](https://gradle.org) for the main intermediary code.

## Status

Accepted.

## Context

Other build tools were considered; [Maven](https://maven.apache.org) is the other big option available to us.  Gradle was chosen over Maven because
- Gradle uses Groovy, an actual programming language, to configure the projects instead of XML (Extensible Markup Language), which has a rigidly defined schema.


- Gradle allows for diversity in configuration of the project.


- Gradle is faster with its job execution.


## Impact

### Positive

- **Advanced Dependency Management** capabilities help streamline the integration of FHIR libraries, such as HAPI FHIR, and other required dependencies


- **Highly Customizable Scripts:** Highly customizable build scripts allow for fine-tuned configuration.


- **Reduced build times**


- **CI/CD Automation:** integrates well with continuous integration and continuous deployment (CI/CD) pipelines, enabling automated testing

### Negative

- **Cumbersome Build Scripts:** Flexibility can lead to complex and verbose build scripts; Managing these scripts can become cumbersome.


- **Configuration Incompatibility:** Custom configurations may not be fully compatible with certain versions of Java or FHIR libraries.

### Risks

- **Possible Slow Build Times** due to scripts and project size

## Related Issues

- #1
- #13
