# 18. SQL Migrations

Date: 2024-02-12

## Decision

We will use Liquibase for SQL Migrations because the rollback functionality is included in the free version, this is not the case with flyway. Liquibase has an open source version and is complemented with a robust set of documentation as well. [Reference](https://docs.liquibase.com/start/home.html)

## Status

Accepted

## Context
As part of having CI/CD pipelines we need tooling inside of the project to allow us to deploy and rollback changes in an automated way. Using Liquibase enables us to store SQL migration scripts inside of the project and update them as needed.  We can then set up the project deployment pipelines to automatically pick up the new changes and make the updates to the database.  Liquibase will also allow for non-manual rollbacks should the need for them arise. As part of this implementation we will be using the Liquibase generated Github Actions in our pipelines to achieve this automation.

## Impact

### Positive

- **Rollback Functionality:** Liquibase’s ability to perform rollbacks in the open-source version ensures we can quickly recover from migration issues without additional cost. 


- **Improved CI/CD Integration:** Automating migrations reduces the need for manual intervention and decreases deployment errors. 


- **Version Control of Schema:** Keeping migration scripts within the project ensures that database changes are tracked alongside application changes, improving traceability. 


- **Extensive Documentation:** Liquibase’s comprehensive documentation simplifies onboarding and troubleshooting.


- **Flexibility:** Liquibase supports a wide range of databases and migration formats, making it adaptable for future needs.


### Negative

- **Learning Curve:** Teams unfamiliar with Liquibase may require time to learn its configuration and scripting syntax.


- **Setup Complexity:** Initial integration with CI/CD pipelines may require extra effort to align with existing workflows. 


- **Dependency Overhead:** Adds a dependency to the project, which must be maintained and updated over time.

### Risks

- **Migration Errors:** Incorrectly defined migration scripts could cause deployment failures or data integrity issues.


- **Rollback Limitations:** Not all changes (e.g., destructive data operations) can be automatically rolled back, requiring careful planning for such scenarios. 


- **Version Drift:** If environments are not consistently updated with migrations, discrepancies could arise between development, staging, and production.


- **GitHub Action Reliability:** Relying on prebuilt GitHub Actions introduces a dependency on external tools, which may have compatibility or update issues over time.


- **Changelog Structure and Dependencies:** A poorly structured changelog can create challenges in managing references between tables. For example, if the `message_link` table references the `received_message_id` column in the `metadata` table, structural changes in `metadata` could require significant changes to the migrations, increasing complexity. This risk can make future schema modifications or rollbacks harder to implement without breaking dependencies https://docs.liquibase.com/start/design-liquibase-project.html.

### Related Issues

- 753
