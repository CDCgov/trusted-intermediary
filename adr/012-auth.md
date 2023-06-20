# 12. Authentication and Authorization

Date: 2023-06-19

## Decision

We will use an AuthN/AuthZ concept similar to ReportStream's "two legged" auth.

## Status

Accepted.

## Context

ReportStream has a form of AuthN/AuthZ that they call "two legged".  It is similar to FHIR's bulk API implementation guide AuthN/AuthZ.  We are inheriting the format.

We chose this because it is exactly what ReportStream does and is accepted practice in the larger FHIR community.  There are a few concepts that ReportStream enforces or uses that we do not because we don't have a requirement for it.  For example, the payload sent to our login endpoint does not require `client_assertion_type`, and we don't send back a `token_expire` since that is encoded into the JWT already.

This form of AuthN/AuthZ goes through multiple steps.
1. Register the client in TI.  The client sends us a public key and we associate it with that client.  This occurs before any API communication.
2. When the client wants to send us information through an API call, they will first call our login endpoint with a payload containing their name and a JWT which is signed by their private key.
3. We validate that the JWT is valid given the public key they sent to us earlier.
4. We generate a short-lived JWT with our own private key and send it back to the client.  This completes the call to the login endpoint.
5. The client then calls our authenticated endpoints with the JWT we sent previously and whatever payload that is required by the specific endpoint.
6. We validate that the JWT is valid given our public (or private) key.
7. We continue with the business of that specific endpoint.

### Related Issues

- #148
