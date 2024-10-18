# 10. Terraform IaC (Infrastructure as Code) for Deploys

Date: 2023-01-04

## Decision

Terraform will be used to deploy our infrastructure into the cloud.

## Status

Accepted.

## Context

Infrastructure as Code (IaC) is an industry best practice to get repeatable and resilient deployments of applications and associated infrastructure into the cloud.

There are many IaC options to deploy to Azure.

- [ARM Templates](https://learn.microsoft.com/en-us/azure/azure-resource-manager/templates/overview).
- [Bicep](https://learn.microsoft.com/en-us/azure/azure-resource-manager/bicep/overview?tabs=bicep).
- [Terraform](https://www.terraform.io).
- [Pulumi](https://www.pulumi.com).
- [Ansible](https://www.ansible.com).
- [Chef](https://www.chef.io).
- [Puppet](https://www.puppet.com).

There are probably even more.

Terraform is multi-cloud and multi-tool, has a full-featured DSL, tracks state and dependencies, and is agentless.
Terraform is also used by ReportStream.

## Impact

### Positive

- **Multi-Cloud Support:** Terraform allows deployment across multiple cloud providers (e.g., AWS, Azure, GCP), providing flexibility and avoiding vendor lock-in. 


- **State Management:** Terraform tracks the state of your infrastructure, which helps prevent configuration drift and makes it easier to manage changes over time.


- **Declarative Language:** The HashiCorp Configuration Language (HCL) is easy to read and write, making it accessible for both developers and operations teams.


- **Community, Ecosystem and Version Control:** Terraform has a large community, a wealth of modules, and an extensive ecosystem of plugins and integrations, making it easier to find solutions and share best practices. Storing Terraform configurations in Git allow teams to track changes and roll them back if needed.


- **Automation and Integration:** Terraform can be integrated into CI/CD pipelines, allowing for automated infrastructure deployments alongside application code.


### Negative


- **State File Management:** Managing the state file can introduce complexities, especially in teams where multiple people may modify infrastructure, leading to potential conflicts or errors.


- **Dependency Management:** Although Terraform manages dependencies between resources, complex infrastructures may require careful planning and a deep understanding of how dependencies interact, which can complicate deployment processes.


### Risks

- **State File Security:** The state file can contain sensitive information, so it's critical to implement proper security measures to protect it (e.g., using remote state storage with access controls).


- **Inconsistencies Across Environments:** Without strict version control and practices in place, discrepancies can arise between different environments (development, staging, production).


- **Vendor Lock-In:** While Terraform itself is multi-cloud, reliance on specific providers' features or modules can lead to some level of vendor lock-in over time.

- **Tool Updates:** Regular updates to Terraform or its providers could introduce breaking changes or require modifications to existing infrastructure code, which might affect ongoing deployments.


### Related Issues

- #76
