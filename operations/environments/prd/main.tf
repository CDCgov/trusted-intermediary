terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.90.0"
    }
  }

  # Use a remote Terraform state in Azure Storage
  backend "azurerm" {
    resource_group_name  = "csels-rsti-prd-moderate-rg"
    storage_account_name = "cdcintermediarytrfrmprd"
    container_name       = "terraform-state"
    key                  = "prd.terraform.tfstate"
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

module "template" {
  source = "../../template/"

  environment = "prd"
  deployer_id = "f5feabe7-5d37-40ba-94f2-e5c0760b4561" //github app registration in CDC Azure Entra
}
