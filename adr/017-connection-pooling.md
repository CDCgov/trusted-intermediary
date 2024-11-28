# 17. Connection Pooling

Date: 2024-02-02

## Decision

We will use HikariCP for connection pooling because of the quality of documentation, and the significant margin in performance above the other available options. It is also still receiving active support and updates from the owners.

## Status

Accepted

## Context
Connection pooling is vital to the project for many reasons including resource management and conservation, performance improvement, latency reduction, scalability, and most importantly here managing connections.

## Impact

### Positive

- **High Performance:** HikariCP outperforms other connection pool libraries in terms of throughput and latency, enhancing application responsiveness.


- **Stability and Reliability:** Active maintenance ensures stability, addressing potential bugs and vulnerabilities. 


- **Ease of Use:** Comprehensive documentation simplifies integration and configuration for developers. 


- **Resource Efficiency:** Limits the number of open connections, reducing load on the database server. 


- **Scalability:** Handles large-scale applications effectively, supporting high concurrency with low overhead. 


- **Enhanced Diagnostics:** Built-in monitoring and logging features facilitate troubleshooting and performance tuning.

### Negative

- **Dependency Overhead:** Adds an external library dependency that must be maintained and updated over time. 


- **Configuration Complexity:** Suboptimal configuration (e.g., pool size, connection timeout) can degrade performance or cause issues under load. 


- **Learning Curve:** Developers unfamiliar with connection pooling or HikariCP may require additional time for onboarding. 


- **Troubleshooting Complexity**: Adds another layer to the stack, potentially complicating debugging of database-related issues.

### Risks

- **Connection Leaks:** Improperly handled connections could lead to resource exhaustion, affecting application stability. 
- Incorrect Configuration: Poorly tuned settings may result in underutilized or overburdened pools. 


- **Driver Compatibility:** Potential issues with certain database drivers if not thoroughly tested. 


- **Operational Challenges:** Monitoring and maintaining the pool in production environments may require additional effort. 


- **Dependency Lock-in:** Switching to another connection pooling library in the future may require significant refactoring.

### Related Issues

- 753
