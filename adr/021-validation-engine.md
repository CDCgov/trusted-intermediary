# 21. Validation Engine

Date: 2024-06-11

## Decision

We will implement a Validation Engine using the [Rules Engine Pattern](https://deviq.com/design-patterns/rules-engine-pattern) to validate incoming FHIR messages based on given conditions. Each validation will be represented as a rule, specifying conditions that must be met and the corresponding failure message if validation fails. Initially, rules will be stored in a JSON file, with considerations on transitioning to a database for scalability.

## Status

Accepted.

## Context

The intermediary needs to validate FHIR messages based on SME research and partner-specific requirements, which are subject to change. The Rules Engine Pattern enables flexible, scalable, and maintainable rule management.

## Impact
**Positive**:
- Easy to update and scale validations. Separation of concerns.

**Negative**:
- Potential complexity in managing rules as they grow.

**Risks**:
- Transition to a database might introduce new challenges.

## Resources

### Related Issues
