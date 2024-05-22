# 1. Architecture Decision Records

Date: 2024-05-17

## Decision

We will use Azure alerts to notify our team of issues with our applications.

## Status

Accepted.

## Context

As part of our CI/CD infrastructure, we need notifications when failures occur.

Current alert is configure to be:
- [Azure Log Search Alerts](https://learn.microsoft.com/en-us/azure/azure-monitor/alerts/alerts-types#log-alerts) for HikariCP Connection failures
  - Trigger on any logged failures with database connections as Azure metrics weren't capturing the issues found in our logs.
  - Stateful (Auto-mitigation), to keep alerts in a `fired` status until resolved and for less noise on frequent/duplicate alerts.
  - Configured to a Slack channel email until our Pagerduty is set up.

### Related Issues

- #1001
