# 7. SBOM (Software Bill of Materials) Generation

Date: 2022-12-07

## Decision

In the context of generating a secure modern application, facing the need to monitor application dependencies and generate a reliable SBOM, we decided to use the [CycloneDX Gradle Plugin](https://github.com/CycloneDX/cyclonedx-gradle-plugin#usage) and we chose not to use [Anchore/Syft](https://github.com/anchore/syft#supported-ecosystems) or Snyk FOSSID for a smoother read.


## Status

Accepted.

## Context

An SBOM (a Software Bill of Materials) is a machine-readable inventory document (that will be generated with each release build) that captures all the software components and dependencies, info on those components, and hierarchical relationships.

## Impact

### Positive
- **Automation in CI/CD:** The CycloneDX Gradle Plugin can seamlessly integrate SBOM generation into the CI/CD pipeline, ensuring the SBOM is always up to date with each release. 


- **Industry Standard:** CycloneDX is widely accepted in the industry, ensuring compatibility with other tools and making compliance with security standards easier. 


- **Vulnerability Scanning:** With the ability to integrate with tools like [Anchore/Grype](https://github.com/anchore/grype#recommended), one can easily add vulnerability scanning to the process. 


- **Format Conversion:** supports integration with Anchore/Syft for SBOM format conversion, providing flexibility to meet different ecosystem requirements.



### Negative

- **Tooling Overhead:** Maintaining additional tooling for SBOM generation (CycloneDX and optional tools like Grype and Syft) could increase the overhead for the project and team, in terms of learning and managing multiple components. 


- **Build Time Impact:** The generation of an SBOM might increase the build time, especially as the project grows in complexity.


### Risks

- **Compliance Dependence:** Relying on CycloneDX as the primary tool for SBOM generation means that any bugs, limitations, or updates to the plugin can impact your compliance process. 


- **Tool Maintenance:** Keeping CycloneDX and its integration with vulnerability scanning tools like Grype up to date _could_ require ongoing maintenance efforts to ensure compatibility and security.


- **Integration with Other Tools:** Future security or compliance needs _could_ require integration with other SBOM standards or tools not supported by CycloneDX, limiting flexibility. 


### Related Issues

- #76
