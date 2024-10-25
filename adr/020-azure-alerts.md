# 20. Azure Alerts

Date: 2024-05-17

## Decision

We will implement Azure Alerts to notify our team of application issues.

## Status

Accepted.

## Context

As part of our CI/CD infrastructure, we need notifications when failures occur.
We chose Azure for alerting because it's built into the infrastructure we're already using,
which gives us easy access to metrics. We're not currently using an external
log aggregation system, so Azure alerts were a much lower lift to implement than
any of the other potential options.

Alerts are configured in [alert.tf](../operations/template/alert.tf). To reduce
unhelpful notifications, we have alerts turned off in the PR environments, so they must
either be tested in `internal` or `dev`, or developers may temporarily turn alerts back on in
their branch's PR environment.

Alerts are sent to email addresses that forward to Slack channels. As of October 2024,
production alerts go to `#production-alerts-cdc-trusted-intermediary` and non-prod alerts
go to `#non-prod-alerts-cdc-trusted-intermediary`.

## Impact

### Positive

- Immediate awareness of critical issues, reducing downtime

### Negative

- Azure's built-in alert options are less robust than some other services - for instance,
they don't have an option for p50/90/99 latency alert. This means we're more limited in
what kinds of alerts we can have
- Navigating from the Azure Slack alerts to the actual logs where issues are occurring
is unintuitive and requires multiple clicks. Even once you find the right logs,
Azure logs lack syntax highlighting and can be hard to read.

### Risks

- Possible alert fatigue if not fine-tuned

## Related Issues

- #1001
