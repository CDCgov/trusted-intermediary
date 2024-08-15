# 22. Transformation Engine

Date: 2024-06-11

## Decision

As with the [Validation Rule Engine](https://github.com/CDCgov/trusted-intermediary/blob/f11891504f5303657cfe1b5c61a515013265f239/adr/021-validation-engine.md), we will use a variation of the [Rules Engine Pattern](https://deviq.com/design-patterns/rules-engine-pattern) to design and implement a transformation rule engine that can transform incoming FHIR messages given a condition. Each transformation will be represented as a rule, specifying conditions that must be met and the corresponding failure message if the transformation fails. Initially, rules will be stored in a JSON file, with considerations on transitioning to a database for scalability.


## Status

Accepted.

## Context

The intermediary needs to transform incoming FHIR messages based on partner-specific requirements, which are subject to change. The Rules Engine Pattern enables flexible, scalable, and maintainable rule management.

## Impact
**Positive**:
- Easy to update and scale validations. Separation of concerns.

**Negative**:
- Potential complexity in managing rules as they grow.

**Risks**:
- Transition to a database might introduce new challenges.

## Resources
