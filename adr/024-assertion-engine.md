# 24. Assertion Engine

Date: 2024-10-02

## Decision

1. We decided to use the Rule Engine framework (used for the transformations and validations) to define the assertions for the ReportStream Integration Test.
2. We decided to refactor the Rule Engine framework to avoid code duplication and add HL7 handling. Before the refactor, the engine was only able to handle FHIR messages.
3. We decided to create our own simple parser for HL7 that will allow us to access segment fields using an index.
4. We decided to create our own HL7 expression syntax and validation, inspired by FHIRPath.

## Status

Accepted.

## Context

While creating the RS Integration Test framework, we needed a way to define the assertions that would be evaluated on the output files.
We decided to use the Rule Engine framework, which is already used for transformations and validations, to define these assertions.
This will allow us to reuse the existing code and make it easier to maintain.

The reasoning behind the decisions in the previous sections is as follows:

1. The Rule Engine framework is already in place and has been proven to work well for transformations and validations.
2. Refactoring the Rule Engine framework will allow us to avoid code duplication and make it easier to maintain.
3. While working with the Hapi library, we found some limitations that made it impossible to access values in the HL7 messages by segment index. The library's typing system doesn't allow to access HL7 fields by index, so we decided to create a very simple parser that would allow us to do that.
4. In order to create assertions, we needed to define a syntax that would allow us to access HL7 fields and compare them. Since we didn't find an existing specification for it, we decided to create our own HL7 expression syntax and validation, following the same patterns and conventions as FHIRPath so it's easier to understand for those familiar with the FHIRPath specifications.

## Impact

For the Assertion Engine framework, similar impact to the Transformation and Validation Engine ADRs can be assumed.

For the HL7 parser and expression validator, this is the expected impact:

### Positive

- We will be able to access HL7 fields by segment index, which will gives us more flexibility and get HL7 fields programmatically.
- We will be able to define assertions for HL7 messages using a simple syntax that is easy to understand and maintain.

### Negative

- We will have to maintain the HL7 parser and expression validator, which could add some overhead to the project.

### Risks

- The HL7 parser and expression validator could introduce bugs that could affect the assertion evaluation.
- The HL7 parser and expression validator may not cover all the cases we need.

## Related ADRs

- [Automated RS Integration Test](025-automated-rs-integration-test.md)
- [Validation Engine](021-validation-engine.md)
- [Transformation Engine](022-transformation-engine.md)
