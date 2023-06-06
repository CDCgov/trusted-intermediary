# Mock Credentials

This folder contains fake credentials and keys used for local testing.  They are not, and must not, be used in a
deployed system.

## Keys

### trusted-intermediary-*

This is a private/public key pair that is used locally for TI.  It may be used in multiple contexts.

### rs-rsa-local-*

This is a private/public key pair that is used locally as a stand-in for ReportStream's keys that we use to
verify the JWT that they send to us is valid.  This is used when ReportStream is sending something to TI, and
ReportStream authenticates to TI's AuthN/AuthZ endpoint first.

## Credentials

### `report-stream-expired-token.jwt`

This is a JWT that has expired and used for testing.
