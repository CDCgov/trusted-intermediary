# 7. SBOM (Software Bill of Materials) Generation

Date: 2022-12-07

## Decision

In the context of generating a secure modern application,
facing the need to monitor application dependencies and generate a
reliable SBOM,

we decided to use the [CycloneDX Gradle Plugin](https://github.com/CycloneDX/cyclonedx-gradle-plugin#usage)
and against the use of Anchore/Syft or Snyk FOSSID

for SBOM generation at release of a version build,
accepting that an SBOM needs to be generated during the CI/CD process


## Status

Accepted.

## Context

An SBOM (Software Bill of Materials) is a machine-readable inventory
document (that will be generated with each release build) that captures
all the software components and dependencies, info on those components,
and hierarchical relationships.
- CycloneDX plugin for gradle can be automated for use with existing CI/CD pipeline
- CycloneDX can work with [Anchore/Syft](https://github.com/anchore/syft#supported-ecosystems) for format converison
- CycloneDX can work with [Anchore/Grype](https://github.com/anchore/grype#recommended) for vulnerability scanning
- CycloneDX is an accepted standard for the industry

### Related Issues

- #76