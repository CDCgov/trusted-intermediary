# 10. Terraform IaC for Deploys

Date: 2023-01-04

## Decision

Terraform will be used to deploy our infrastructure into the cloud.

## Status

Accepted.

## Context

Infrastructure as Code (IaC) is a industry best practice to get repeatable and resilient deployments of applications and
associated infrastructure into the cloud.

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


### Related Issues

- #76
