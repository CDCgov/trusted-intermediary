# 21. Validation Engine

Date: 2024-06-11

## Decision

We will implement a Validation Engine using the [Rules Engine Pattern](https://deviq.com/design-patterns/rules-engine-pattern) to validate incoming FHIR messages, based on given conditions. Each validation will be represented as a rule, specifying conditions that must be met, and the corresponding failure message if validation fails.

Initially, rules will be stored in a JSON resource file, but the plan is to migrate them to a database or or other type of external storage in the future, which would allow to update the rules without the need for deploying the application.

## Status

Accepted.

## Context

The intermediary needs to validate FHIR messages based on SME research and partner-specific requirements, which are subject to change. The Rules Engine Pattern enables flexible, scalable, and maintainable validation management.

## Impact

### Positive

- Easy to update and scale validations. Easy to refine scope for validations (general or partner-specific)
- Validation rules could be reused by multiple partners.
- It should make it easier to add a UI in the future, which could potentially allow partners to self-serve and add their own validations.
- The framework can be leveraged to implement transformations as well.
- Separation of concerns and code reusability.
- It's modular enough that in the future we could decide to spin-off the Validation Engine into its own microservice that could be used to validate any FHIR (and potentially HL7) message.

### Negative

- There's added overhead code for the engine, but this code should seldom be required to maintain compared to the actual rules which should be a lot easier to maintain.
- We'll need to deploy the application to have any changes to the validations available in production, at least until we migrate to a database or external storage.

### Risks

- The engine will need to iterate over all rules to decide which ones to apply, which could impact performance if the rules grow substantially.
- If the conditions are not well defined there could be potential leakage (validations misapplied), but this should be identified in staging while onboarding.
