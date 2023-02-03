# 11. OWASP ZAP Dynamic Application Security Testing

Date: 2023-01-06

## Decision

We will use [OWASP ZAP](https://www.zaproxy.org/) for DAST scanning of our application.

## Status

Accepted.

## Context

Zed Attack Proxy (ZAP) is a free, open-source penetration testing tool being maintained under the umbrella of the Open Web Application Security Project (OWASP). ZAP is designed specifically for testing web applications and is both flexible and extensible.
- Has Docker image which allows for an isolated testing environment.
- It is open source and widely used.
- Provides crossed-platform.
- Has extensive community support.
- Can generate reports.

[Reference](https://www.zaproxy.org/getting-started/)

### Related Issues

- #77
