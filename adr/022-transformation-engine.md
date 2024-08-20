# 22. Transformation Engine

Date: 2024-06-11

## Decision

As with the [Validation Rule Engine](021-validation-engine.md), we will use a variation of the [Rules Engine Pattern](https://deviq.com/design-patterns/rules-engine-pattern) to design and implement a transformation rule engine that can transform incoming FHIR messages, given a condition. Each transformation will be represented as a rule, specifying conditions that must be met, with a name and arguments. The name of the rule will match a Java function in [/etor/src/main/java/gov/hhs/cdc/trustedintermediary/etor/ruleengine/transformation/custom/](/etor/src/main/java/gov/hhs/cdc/trustedintermediary/etor/ruleengine/transformation/custom/) that will apply the transformation, using the given arguments.

Failed transformations will not interrupt the flow of the message. Any transformations that have errors will be skipped instead, making sure to log the error.

Initially, rules will be stored in a JSON resource file, but the plan is to migrate them to a database or external storage in the future, which would allow to update the rules without the need for deploying the application.

## Status

Accepted.

## Context

The intermediary needs to transform incoming FHIR messages based on partner-specific requirements, which are subject to change. The Rules Engine Pattern enables flexible, scalable, and maintainable management for transformations.

## Impact

### Positive

- Easy to update and scale transformations. Easy to refine scope for transformations (general or partner-specific)
- Transformation rules could be reused by multiple partners.
- It should make it easier to add a UI in the future, which could potentially allow partners to self-serve and add their own transformations.
- Leverages on existing code from the Validation Rules Engine.
- Separation of concerns and code reusability.

### Negative

- There could potentially be conflicts between rules. Order of execution matters.
- There's added overhead code for the engine, but this code should seldom be required to maintain compared to the actual rules which should be a lot easier to maintain.
- We'll need to deploy the application to have any changes to the transformations available in production, at least until we migrate to a database or external storage.

### Risks

- The engine will need to iterate over all rules to decide which ones to apply, which could impact performance if the rules grow substantially.
- If the conditions are not well defined there could be potential leakage (transformations misapplied), but this should be identified in staging while onboarding.
