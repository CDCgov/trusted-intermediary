# 23. Deployment Slots for Zero Downtime Deploys

Date: 2024-09-03

## Decision
We will use Azure Web App Deployment Slots to facilitate zero-downtime deploys of the TI app

## Status

Accepted.

## Context
Because TI is driven by web traffic from ReportStream, we can receive http calls at any time.
If TI fails to respond, ReportStream will have to try sending the data again later, causing delays.
By implementing zero-downtime deploys, our service can remain available to any incoming calls.

## Impact
### Positive
- **Zero-downtime deploys**: Zero-downtime deploys keep us from dropping incoming calls during deployment.
- **Easy rollback**: Deployment slots make it easy to roll back to the previous version of the
app if we find errors after deploy.
- **Consistency**: Deployment Slots are an Azure feature specifically designed to enable
zero-down time deployment. We use deployment slots in all TI environments and
in the SFTP Ingestion Service.

### Negative
- **Incomplete support for Linux**: The auto-swap feature is not available for Linux-based web apps like ours.
 so we had to include an explicit swapping step in our updated deployment process.
- **Opaque responses from `az webapp deployment slot swap` CLI**: When there are issues swapping slots, the CLI doesn't
return any details about the issue. The swapping operation can also take as much as 20 minutes
to time out if there's a silent failure, which slows down deploy and validation.
- **Steep learning curve**: Most of the official docs and unofficial resources
(such as blogs and tutorials) for deployment slots are written for people using Windows
servers and Microsoft-published programing languages. This lack of support for other platforms
and languages means a lot more trial and error is involved.

### Risks
- Because of the incomplete support for and documentation of our usecase, we may not have
chosen the optimal implementation of this feature. It may also be time-consuming to
troubleshoot if we run into future issues.
