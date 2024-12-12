# 16. Postgres Database

Date: 2024-01-08

## Decision

We will use a Postgres database for storage of project data that needs to persist across restarts.

## Status

Accepted

## Context

Postgres is an open-source, relational, SQL database. It provides a proven and robust feature set in the base implementation as well as add ons if specific features are required. Postgres is also well-supported by the community.
[Reference](https://www.postgresql.org/docs/)

## Impact


### Positive


- **Reliable Data Storage:** Ensures that project data is safely persisted across application restarts. 


- **Rich Feature Set:** Provides advanced capabilities (e.g., ACID compliance, JSONB, and indexing options) to handle diverse requirements. 


- **Community and Ecosystem:** Extensive libraries, tools, and community support minimize development effort and troubleshooting time.


### Negative


**Backup and Restore Challenges:** Large databases can make backups and restores resource-intensive and time-consuming, impacting operational readiness during failures.

**Cost of High Availability:** Achieving high availability (e.g., via replication, clustering) may require additional infrastructure, tools, and management, leading to increased operational costs.

**Security Management Overhead:** Properly securing a Postgres instance, including managing user permissions, network access, and encryption, requires dedicated effort. Misconfigurations could lead to vulnerabilities.


### Risks


- **Data Integrity Risks:** Poorly managed schema migrations or incorrect configurations could lead to data loss or corruption.


- **Performance Bottlenecks:** Inefficient queries or lack of proper indexing might result in slow response times as data grows. 


- **Operational Downtime:** Mismanagement of updates, backups, or scaling operations could lead to downtime or data unavailability.


### Related Issues

- 672
