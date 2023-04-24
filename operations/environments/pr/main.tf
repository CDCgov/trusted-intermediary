terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.53.0"
    }
  }

  # Use a remote Terraform state in Azure Storage
  backend "azurerm" {
    resource_group_name  = "cdcti-terraform"
    storage_account_name = "cdctiterraform"
    container_name       = "tfstate"
  }
}

# Configure the Microsoft Azure Provider
provider "azurerm" {
  features {}
}

module "template" {
  source = "../../template/"

  environment = "pr${var.pr_number}"
}
