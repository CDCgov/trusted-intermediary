# 2. Java

Date: 2022-10-21

## Decision

We will use Java 17 for the main intermediary code.

This doesn't preclude us from using other languages for other services or applications.

## Status

Accepted.

## Context

We decided to use a Java Virtual Machine- (JVM) based language because it has many third-party healthcare libraries and frameworks that are pertinent to the project. 

We decided on Java over Kotlin, Scala, and Groovy because more people know Java.

## Impact

### Positive

- **Robust and Well-Maintained Libraries:** has robust libraries, like HAPI FHIR, which provide comprehensive support for creating, parsing, and validating FHIR resources. Libraries are well-maintained and widely used in the healthcare industry


- **Powerful Data Processing:** ability to handle large-scale, multithreaded applications makes it suitable for processing large volumes of FHIR data in real-time, which is crucial in healthcare settings


- **Compliant Security Features:** Java's security features, such as encryption and secure authentication mechanisms, help in building HIPAA-compliant FHIR applications.

### Negative

- **Verbose Code:** Code tends to be more verbose leading to longer development times and more boilerplate code.


### Risks

- **Limited Functional Expression:** Limited functional programming capabilities can make it harder to express certain data transformation logic that is common in FHIR processing, compared to languages with stronger functional programming support.

### Related Issues

- #1
- #13
