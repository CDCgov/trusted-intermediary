# 13. Wrapped Domain Objects

Date: 2023-06-19

## Decision

We will "wrap" the HAPI FHIR (or any other FHIR library we may pivot to) resource classes with an interface to prevent the HAPI library from "polluting" our business logic.

## Status

Accepted.

## Context

We are wanting to adhere to the OeSA principles, and one of them is that less important code depends on more important code.  Third party dependencies are less important that our business logic.  But how do we isolate the HAPI's FHIR resource classes from our business logic?  The HAPI FHIR library adds a bunch of value and allows us from not writing our own FHIR library, so we definitely want to use it.  We can "wrap" it in an interface.

This interface uses Java's generics feature to communicate that there is some underlying class to back up the interface.  This interface has at least one method on it that gets the underlying object.  The interface can specify other methods as needed by the business logic.  We then implement this interface with a custom class that uses the HAPI FHIR resource classes behind the scenes.

Our business logic just uses the interface without any need to know what the underlying implementation is using type errasure.  When we need to actually work with the underlying implementation, we are already in a level of abstraction that can know that we use HAPI.

You can see an example of this by searching for `Demographics.java` (the interface) and `HapiDemographics.java` (the implementation).

### Related Issues

- #79
- [ADR 5 - Option-enabling Software Architecture (OeSA)](./005-oesa.md)
