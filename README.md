# CDC Trusted Intermediary

This document provides comprehensive instructions for setting up, running, and contributing to the project.

---

## Table of Contents

1. [Requirements](#requirements)
1. [Running the Application](#running-the-application)
1. [Development Guide](#development-guide)
   - [Additional Requirements](#additional-requirements)
   - [Environment Setup](#environment-setup)
   - [Using a Local Database](#using-a-local-database)
   - [Compiling](#compiling)
   - [Testing](#testing)
      - [Unit Tests](#unit-tests)
      - [End-to-End Tests](#end-to-end-tests)
      - [Load Testing](#load-testing)
   - [Debugging](#debugging)
1. [Deploying](#deploying)
   - [Environments](#environments)
   - [Initial Azure and GitHub Configuration](#initial-azure-and-github-configuration)
   - [Interacting with Deployed Environments](#interacting-with-deployed-environments)
1. [Pre-Commit Hooks](#pre-commit-hooks)
1. [Contributing](#contributing)
1. [Setup with ReportStream](#setup-with-reportstream)
1. [DORA Metrics](#dora-metrics)
1. [Related Documents](#related-documents)
1. [CDC Notices](#cdc-notices)
   - [Public Domain Notice](#public-domain-notice)
   - [License Notice](#license-notice)
   - [Privacy Notice](#privacy-notice)
   - [Records Management Notice](#records-management-notice)
1. [Troubleshooting](#troubleshooting)

---

## Requirements

- **Java**: Any distribution of Java 17 JDK.
- [Docker](https://www.docker.com/) or [Podman](https://podman.io/)

---

## Running the Application

To run the application, use the command:

`./gradlew clean run`

- **Default Port**: 8080.
- **Data Handling**: Reads/writes data to a local file unless a database is configured.

### Accessing the API Documentation

API documentation is available at `/openapi`.

---

## Development Guide

### Additional Requirements

To contribute to development, install:


- [Locust.io](https://docs.locust.io/en/stable/installation.html)
- [Python](https://docs.python-guide.org/starting/installation/)
- [Terraform](https://www.terraform.io)
- [Liquibase](https://www.liquibase.com/download)

#### Pre-commit
- [Pre-Commit](https://pre-commit.com)

Install pre-commit hooks by running:

`pre-commit install`

### Environment Setup

Run the `generate_env.sh` script to create the `.env` file:

`./generate_env.sh`

For Docker users, update `.env` values for database and port configuration as needed.

### Using a Local Database

1. Use `docker-compose.yml` to start the local database.
2. Apply migrations using `liquibase` with appropriate configuration for your platform (single vs. double quotes as needed).

### Compiling

Build the application by running:

`./gradlew shadowJar`

The compiled artifact will be located at `/app/build/libs/app-all.jar`.

---

## Testing

### Unit Tests

Run unit tests:

`./gradlew clean allUnitTests`

### End-to-End Tests

Run end-to-end tests:

`./gradlew e2e:clean e2e:test`

To automate testing:

`./e2e-execute.sh`

### Load Testing

Run load tests using Locust:

- `./gradle-load-execute.sh`
- `./docker-load-execute.sh`

For interactive mode, use:

`locust -f ./operations/locustfile.py`

---

## Debugging

### IntelliJ JVM Debugging

- A pre-configured `Debug TI` remote JVM configuration is available.
- To use Java Debug Wire Protocol (JDWP), modify the `Dockerfile` to include the required options.

---

## Deploying

### Environments

1. **Internal**: Free for testing; deployed to a non-CDC domain.
2. **Dev**: Similar to Internal but deployed to CDC domain.
3. **Staging**: Production-like, stable, requires PR review.
4. **Prod**: Production environment; deployed on release.

### Initial Azure and GitHub Configuration

Set up Azure and GitHub for new environments by creating required resources, configuring App Registrations, and adding secrets.

### Interacting with Deployed Environments

For environments with firewall restrictions, add your IP to the allowlist using the Azure portal. Remove it once done.

---

## Contributing

Follow [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines. Contributions are under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

---

## Setup with ReportStream

Detailed setup for interacting with ReportStream is outlined in the [ReportStream docs](rs-e2e/README.md).

---

## DORA Metrics

Metrics tracked include Deployment Frequency, Change Fail Rate, and Mean Time to Recovery. Metrics are generated weekly and accessible in GitHub Actions artifacts.

---

## Related Documents

- [Open Practices](open_practices.md)
- [Rules of Behavior](rules_of_behavior.md)
- [Disclaimer](DISCLAIMER.md)
- [Code of Conduct](code-of-conduct.md)

---

## CDC Notices

### Public Domain Notice

This repository is public domain and not subject to domestic copyright.

### License Notice

Licensed under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

### Privacy Notice

See [CDC Privacy Policy](http://www.cdc.gov/other/privacy.html).

### Records Management Notice

This repository is not a source of government records.

---

## Troubleshooting

### Missing JAR Error

If you encounter:

`Error: copier: stat: "/app/build/libs/app-all.jar": no such file or directory`

Run:

`./gradlew shadowJar`
