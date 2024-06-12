# 21. Validation Engine

Date: 2024-06-11

## Decision

We will use a variation of the [Rules Engine Pattern](https://deviq.com/design-patterns/rules-engine-pattern) to implement a Validation Rules Engine that will validate incoming FHIR messages, given a condition.

The validations will be stored in the form of rule definitions that can be executed by the Validation Engine. The rule definitions will also have conditions that should be met for the validation to apply to the incoming message, and a message that will be shown when the validation fails.

Initially, the validation definitions will be store in a JSON file, but we will consider using a database to store the rules in the future.

## Status

Accepted.

## Context

The intermediary needs to be able to validate incoming messages based on requirements that could be universal or specific to partners. The validations are defined by the research done by SMEs and the requirements of the partners, so it's expected that we'll need to add and modify validations over time.

The Rules Engine Pattern is a good fit for this problem because it allows us to define the rules in a way that is easy to understand and modify, and it also allows us to execute the rules in a way that is efficient and scalable.

## Resources
