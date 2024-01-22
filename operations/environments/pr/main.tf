terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.88.0"
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
  features {
    key_vault {
      purge_soft_deleted_secrets_on_destroy = false
    }
  }
}

resource "azurerm_resource_group" "group" { //create the PR resource group because it has a dynamic name that cannot be always pre-created
  name     = "csels-rsti-pr${var.pr_number}-moderate-rg"
  location = "East US"
}

module "template" {
  source = "../../template/"

  environment = "pr${var.pr_number}"
  deployer_id = "d59c2c86-de5e-41b7-a752-0869a73f5a60" //github app registration in Flexion Azure Entra

  depends_on = [azurerm_resource_group.group]
}
