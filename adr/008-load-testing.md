# 8. Load Testing

Initial date: 2022-12-21
Updated: 2025-01-07

## Decision

In the context of maintaining quality of service and the need to consistently monitor and address performance concerns, we decided to use [Locust.io](https://locust.io/) instead of [Artillery.io](https://www.artillery.io/) or [JMeter](https://jmeter.apache.org/) for load testing. We accept that performance testing needs to be completed as part of the definition of done.


## Status

Accepted.

## Context

Load Testing will assist in determining the performance of a system under
real-life load conditions, both normal and extreme.

In January 2025, we added the capability to run load tests automatically on a schedule in Azure.
In the deployed load tests, we're hitting mock ReportStream endpoints. This allows us to 1) not
bombard ReportStream with unexpected traffic and 2) identify performance issues that are specific
to the Intermediary.

## Impact

### Positive

- **Scalability:** Locust.io is highly scalable and can simulate millions of users, making it ideal for both small-scale and large-scale load tests.


- **Python-Based:** Writing tests in Python allows for flexibility and ease of use, especially for teams already familiar with the language. 


- **Cost Efficiency:** Locust.io is open-source, and doesn’t require licensing fees, which can reduce the overall cost of performance testing.


- **Azure:** Running the load tests on a schedule in a more realistic enviornment gives us more consistent data


### Negative

- **Limited Features:** Compared to more feature-rich tools, Locust.io might lack advanced performance monitoring or detailed reporting features. 


- **Manual Script Writing:** Test scripts need to be manually written, which could be more time-consuming compared to GUI-based test creation offered by tools like JMeter or Artillery.io.


### Risks

- **Scalability Complexity:** While Locust.io is scalable, setting up distributed testing across multiple machines can introduce complexity, especially for teams with limited experience in handling distributed systems. 


- **Tool Ecosystem:** If future testing needs expand beyond Locust.io's capabilities, there may be a need to switch or integrate with other tools, leading to additional complexity or costs. 


- **Resource Usage:** Running large-scale tests using Locust.io may require significant system resources, which could impact cost and infrastructure planning.


- **Locust Future in Azure is Uncertain:** We were able to create a Locust test in Azure in November 2024, but as of January 2025, were unable to create another one. We've submitted a bug report, but if we remain unable to create Locust tests in Azure, we won't be able to expand this test setup to other environments


- **Azure Load Testing Cannot be Terraformed:** Since Azure Load Testing resources can't be created/managed in Terraform, they must be created manually. This is more work and more error prone


### Related Issues

- #76, #1122 
