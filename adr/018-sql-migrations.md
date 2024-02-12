# 18. SQL Migrations

Date: 2024-02-12

## Decision

We will use Liquibase for SQL Migrations because the rollback functionality is included in the free version, this is not the case with flyway. Liquibase has an open source version and is complemented with a robust set of documentation as well. [Reference](https://docs.liquibase.com/start/home.html)

## Status

Accepted

## Context
As part of having CI/CD pipelines we need tooling inside of the project to allow us to deploy and rollback changes in an automated way. Using Liquibase enables us to store SQL migration scripts inside of the project and update them as needed.  We can then set up the project deployment pipelines to automatically pick up the new changes and make the updates to the database.  Liquibase will also allow for non-manual rollbacks should the need for them arise. As part of this implementation we will be using the Liquibase generated Github Actions in our pipelines to achieve this automation.


### Related Issues

- 753
