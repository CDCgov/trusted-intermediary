# 8. Load Testing

Date: 2022-12-21

## Decision

In the context of maintaining quality of service and the need to consistently monitor and address performance concerns, we decided to use [Locust.io](https://locust.io/) instead of [Artillery.io](https://www.artillery.io/) or [JMeter](https://jmeter.apache.org/) for load testing. We accept that performance testing needs to be completed as part of the definition of done.


## Status

Accepted.

## Context

Load Testing will assist in determining the performance of a system under
real-life load conditions, both normal and extreme.

## Impact

### Positive

- **Scalability:** Locust.io is highly scalable and can simulate millions of users, making it ideal for both small-scale and large-scale load tests. 


- **Python-Based:** Writing tests in Python allows for flexibility and ease of use, especially for teams already familiar with the language. 


- **Cost Efficiency:** Locust.io is open-source, and doesn’t require licensing fees, which can reduce the overall cost of performance testing.


### Negative

- **Limited Features:** Compared to more feature-rich tools, Locust.io might lack advanced performance monitoring or detailed reporting features. 


- **Manual Script Writing:** Test scripts need to be manually written, which could be more time-consuming compared to GUI-based test creation offered by tools like JMeter or Artillery.io.


### Risks

- **Scalability Complexity:** While Locust.io is scalable, setting up distributed testing across multiple machines can introduce complexity, especially for teams with limited experience in handling distributed systems. 


- **Tool Ecosystem:** If future testing needs expand beyond Locust.io's capabilities, there may be a need to switch or integrate with other tools, leading to additional complexity or costs. 


- **Resource Usage:** Running large-scale tests using Locust.io may require significant system resources, which could impact cost and infrastructure planning.


### Related Issues

- #76
