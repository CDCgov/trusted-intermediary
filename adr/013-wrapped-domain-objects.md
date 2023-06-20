# 13. Wrapped Domain Objects

Date: 2023-06-19

## Decision

We will "wrap" the HAPI FHIR (or any other FHIR library we may pivot to) resource classes with an interface to prevent the HAPI library from "polluting" our business logic.

## Status

Accepted.

## Context

Per [ADR 5](./005-oesa.md), one of the principles to follow is that less important code should depend upon more important code.  Third party dependencies are less important than our business logic, so how do we isolate the HAPI library's resource classes from our business logic?

We typically isolate libraries by creating an Interface at the library boundary.  To take advantage of Java's type safety we define the interface using Generic types to ensure that the correct classes are used.  On a wrapper Interface we always include a method to retrieve the underlying object.  Extra methods are defined based on business needs.  The implementation of this interface is customized to use the underlying third party library.

Our business logic can then use the wrapper Interface without needing to know the actual underlying implementation, and that implementation can be changed without changing the Interface that the business logic relies on.

You can see an example of this in [Demographics.java](../app/src/main/java/gov/hhs/cdc/trustedintermediary/etor/demographics/Demographics.java) (the interface) and [HapiDemographics.java](../app/src/main/java/gov/hhs/cdc/trustedintermediary/external/hapi/HapiDemographics.java) (the implementation).

### Related Issues

- #79
- [ADR 5 - Option-enabling Software Architecture (OeSA)](./005-oesa.md)
