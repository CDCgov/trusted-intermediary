# 9. Docker Containerization

Date: 2022-01-04

## Decision

We will use [Docker](https://docs.docker.com/get-started/overview/) for containerization of our application.

- [Reference](https://docs.docker.com/get-started/overview/)

## Status

Accepted.

## Context

Docker is an open platform for developing, shipping, and running applications. Docker provides the ability to package and run an application in a loosely isolated environment. Some benefits of using Docker:


## Impact

### Positive

- **Compatibility:** Docker containers can run consistently across various environments (development, testing, production), reducing compatibility issues and making it easier to move applications between different environments.


- **Isolation:** Each container runs in its own isolated environment, which minimizes conflicts between applications and simplifies dependency management.


- **Scalability:** Docker supports scaling applications easily through container orchestration tools like Kubernetes, allowing for better resource management and load handling.


- **Rapid Development:** Containers can be quickly built, tested, and deployed, accelerating the development cycle and improving productivity.


- **Ecosystem and Community:** Docker has a large and active community, providing a wealth of resources, tools, and best practices that can help in troubleshooting and optimization.


### Negative

- **Complexity in Orchestration:** managing a large number of containers can introduce complexity, especially without proper orchestration tools.


- **Security Considerations:** Containers share the same OS kernel, which can pose security risks if not properly managed (e.g., vulnerability exposure).


### Risks


- **Vendor Lock-In:** Relying on specific Docker features or services might lead to challenges in migrating to other containerization solutions in the future.
  

- **Configuration Drift:** As applications evolve, keeping container configurations consistent across different environments can be challenging, potentially leading to discrepancies.
  

- **Data Persistence:** Managing data persistence for stateful applications can be complicated with Docker containers, requiring additional solutions like volumes or external databases.


### Related Issues

- #76
