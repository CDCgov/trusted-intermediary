# 20. Azure Alerts

Date: 2024-05-17

## Decision

We will implement Azure Alerts to notify our team of application issues.

## Status

Accepted.

## Context

As part of our CI/CD infrastructure, we need notifications when failures occur.

To ensure rapid response to application failures within our CI/CD infrastructure, we require real-time notifications for critical issues. The current alert setup focuses on:

- **Type:** [Azure Log Search Alerts](https://learn.microsoft.com/en-us/azure/azure-monitor/alerts/alerts-types#log-alerts) for HikariCP connection failures.


- **Trigger:** Any logged failures with database connections.


- **Configuration:** Alerts are stateful (auto-mitigation); set to `fired` status to reduce noise from frequent or duplicate alerts.


- **Notification:** Alerts sent to a Slack channel via email until PagerDuty is operational.

## Impact

### Positive

- Immediate awareness of critical issues, reducing downtime

### Negative

- Possible alert fatigue if not fine-tuned

### Risks

- None

## Related Issues

- #1001
