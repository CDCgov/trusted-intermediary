# 8. Load Testing

Date: 2022-12-21

## Decision

In the context of maintaining quality of service,
facing the need to consistently monitor and address performance concerns

we decided to use the [Locust.io](https://locust.io/)
and against the use of [Artillery.io](https://www.artillery.io/)
or [JMeter](https://jmeter.apache.org/)

for load testing, accepting that performance testing needs to completed
as a part of the definition of done.

## Status

Accepted.

## Context

Load Testing assists in determining the performance of a system under
real-life load conditions, both normal and extreme.
 - Locust.io is free, open-source, scalable
 - Tests written in Python
 - Works with gRPC servers
 - Easy to use UI
 - Simple setup and task configuration

### Related Issues

- #76
