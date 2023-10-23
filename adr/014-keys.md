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

### Related Issues

- #584
