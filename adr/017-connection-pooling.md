# 17. Connection Pooling

Date: 2024-02-02

## Decision

We will use HikariCP for connection pooling because of the quality of documentation, and the significant margin in performance above the other available options. It is also still receiving active support and updates from the owners.

## Status

Accepted

## Context
Connection pooling is vital to the project for many reasons including resource management and conservation, performance improvement, latency reduction, scalability, and most importantly here managing connections.


### Related Issues

- 753
