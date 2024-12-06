# 12. Authentication and Authorization

Date: 2023-06-19

## Decision

We will use an AuthN/AuthZ concept similar to ReportStream's "two legged" auth. This model uses JSON Web Tokens (JWT) for secure client authentication and authorization across API interactions.

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


## Impact

### Positive

- **Enhanced Security:** AuthN/AuthZ ensures that only authorized users can access specific resources, reducing the risk of unauthorized access. 


- **Defined Access Control:** Allows for fine-grained control over which users or roles can access certain endpoints or resources, enabling a more flexible and secure application. 


- **Auditing and Compliance:** provides mechanisms for tracking user access and actions within the API, which helps meet regulatory requirements (e.g., GDPR, HIPAA).
  

- **Scalability:** With proper token management (e.g., OAuth2, JWT), it is easier to scale applications while maintaining secure authentication and authorization mechanisms.


### Negative

- **Complexity:** Adding AuthN/AuthZ layers increases the complexity of the system, which can lead to higher development and maintenance costs.


- **Performance Overhead:** Authentication and authorization checks can introduce latency, especially in distributed systems where tokens need to be validated at every request.
  

- **Misconfiguration:** Incorrect configuration of scopes, permissions, or roles could inadvertently allow unauthorized access to sensitive data.


### Risks

- **Vendor Lock-in:** Using third-party AuthN/AuthZ services (e.g., Auth0, AWS Cognito) may lead to vendor lock-in, making it harder to switch providers or implement custom solutions.


- **Security Vulnerabilities:** Weak implementation can introduce vulnerabilities like token theft, replay attacks, or misconfigured access policies that expose sensitive resources.


- **Token Expiration and Revocation:** Improper handling of token expiration and revocation could result in unauthorized access or frustrated users if tokens expire too quickly.
  

- **Regulatory Compliance Risk:** Failing to meet security and privacy regulations (e.g., encrypting tokens, securely storing user credentials) could lead to legal penalties.


- **System Downtime:** If the authentication server fails or is slow to respond, it could bring down the entire API, making the application unavailable.


### Related Issues

- #148
