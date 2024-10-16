# 26. HL7 Test Message Routing

Date: 2024-10-10

## Decision

We will use `MSH-11` to identify and route test and non-test messages in ReportStream. The values for `MSH-11` will be:
- `T` (Training): for test files to be sent manually to partners
- `D` (Debugging): for test files **not** to be sent to partners, and sent manually
- `N` (Non-Production Testing): for test files **not** to be sent to partners, and sent by a scheduled task

## Status

Accepted.

## Context

Some of the transformations we apply in the Intermediary overwrite `MSH-5` (Receiving Application) and `MSH-6` (Receiving Facility), which are normally used in HL7 for routing purposes. That's currently the case for UCSD transformations. Because of this, we can't rely on those fields to identify and route test messages in ReportStream.

After internal discussion, we decided to use `MSH-11` (Processing ID) for this purpose as it is a field already used in HL7 to identify test messages.

We decided to use `D` (Debugging) for internal test messages and `T` (Training/Testing) for partner test messages because `T` is already used by our partners for testing, so we need to stick to that value.

For routing purposes in ReportStream, `N` should only be relevant in non-prod environments, where we have scheduled tasks sending test messages. For both prod and non-prod, we expect to use both `D` and `T`, so the `MSH-11` routing filters for those values can be the same in all environments.

## Impact

### Positive

- We will be able to identify and route test messages in ReportStream without relying on fields that are overwritten by transformations; and as a result, avoid sending test messages to partners by mistake
- We will be able to differentiate between test messages that are sent manually and scheduled (e.g. the RS Integration Tests)
- We will have a consistent way to identify test messages across all partners, regardless of the transformations applied

## Negative

- We will have to make sure to update our internal test message to use `D` in `MSH-11`
- We will have to make sure to update the RS Integration Tests messages to use `N` in `MSH-11`

### Risks

- We may forget to update the `MSH-11` value for messages meant for internal testing, which could lead to test messages being sent to partners by mistake

## Related Issues

#1387
