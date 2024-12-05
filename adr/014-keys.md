# 14. Keys

Date: 2023-10-23

## Decision

### Internal Keys

These keys are used for the intermediary's internal use.  These keys will follow the following naming paradigm...

`trusted-intermediary-<key type>-key-<environment>`.

For example, `trusted-intermediary-private-key-staging` or `trusted-intermediary-public-key-prod`.

### Organization Keys

These are keys for external orgs to authenticate with us. Currently, report stream is the only organization we have. The pattern for the name of these keys is

`organization-<org name>-<key type>-key-<environment>`.

For example, `organization-report-stream-public-key-staging`

## Status

Accepted.

## Context

This naming convention applies to all locations where our keys are stored.  Previously, we didn't have a consistent naming convention across all our locations which caused confusion on which key was to be used in which context and environment.


## Impact


### Positive

- **Clarity and Consistency:** Standardized naming ensures keys are easily identifiable and reduces misconfigurations.


- **Improved Operations:** Teams will spend less time resolving key-related issues.


- **Scalability:** As more organizations integrate, the naming convention will simplify management and avoid duplication.

### Negative

- **Migration Effort:** Renaming existing keys and updating references in systems may require a one-time effort.


### Risks

- **Human Error in Implementation:** Incorrectly applying the naming convention during key creation or migration could lead to confusion or outages.


- **Lack of Enforcement:** Without clear processes or automation, teams might unintentionally deviate from the convention.


- **Backward Compatibility:** Older systems or scripts may fail if they rely on the previous key names.

### Related Issues

- #584
