# 2. Java

Date: 2022-10-21

## Decision

We will use Java 17 for the main trusted intermediary code.
This doesn't preclude us from using other languages for other services or applications.

## Status

Accepted.

## Context

We decided to use a JVM based language because it has many third-party healthcare libraries and frameworks.

We decided on Java over Kotlin, Scala, and Groovy because more people know Java.

## Impact

### Positive

- has robust libraries, like HAPI FHIR, which provide comprehensive support for creating, parsing, and validating FHIR resources.
- libraries are well-maintained and widely used in the healthcare industry.
- ability to handle large-scale, multi-threaded applications makes it suitable for processing large volumes of FHIR data in real-time, which is crucial in healthcare settings.
- Java's security features, such as encryption and secure authentication mechanisms, help in building HIPAA-compliant FHIR applications.

### Negative

- code tends to be more verbose leading to longer development times and more boilerplate code.
- code tends to be more verbose which can lead to longer development times and more boilerplate code.

### Risks

- limited functional programming capabilities can make it harder to express certain data transformation logic that is common in FHIR processing, compared to languages with stronger functional programming support.

### Related Issues

- #1
- #13
