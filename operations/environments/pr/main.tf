terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.16.0"
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
      purge_soft_deleted_keys_on_destroy    = false
    }
  }
}

resource "azurerm_resource_group" "group" { //create the PR resource group because it has a dynamic name that cannot be always pre-created
  name     = "csels-rsti-pr${var.pr_number}-moderate-rg"
  location = "East US"
}



module "template" {
  source = "../../template/"

  environment       = "pr${var.pr_number}"
  deployer_id       = "d59c2c86-de5e-41b7-a752-0869a73f5a60" //github app registration in Flexion Azure Entra
  alert_slack_email = var.alert_slack_email

  depends_on = [
    azurerm_resource_group.group,
    azurerm_virtual_network.vnet,
    azurerm_route_table.database,
    azurerm_route.entra_internet,
    azurerm_network_security_group.db_security_group,
    azurerm_network_security_rule.DB_Splunk_UF_omhsinf,
    azurerm_network_security_rule.DB_Splunk_Indexer_Discovery_omhsinf,
    azurerm_network_security_rule.DB_Safe_Encase_Monitoring_omhsinf,
    azurerm_network_security_rule.DB_ForeScout_Manager_omhsinf,
    azurerm_network_security_rule.DB_BigFix_omhsinf,
    azurerm_network_security_rule.DB_Allow_All_Out_omhsinf,
    azurerm_network_security_group.app_security_group,
    azurerm_network_security_rule.App_Splunk_UF_omhsinf,
    azurerm_network_security_rule.App_Splunk_Indexer_Discovery_omhsinf,
    azurerm_network_security_rule.App_Safe_Encase_Monitoring_omhsinf,
    azurerm_network_security_rule.App_ForeScout_Manager_omhsinf,
    azurerm_network_security_rule.App_BigFix_omhsinf,
    azurerm_network_security_rule.App_Allow_All_Out_omhsinf,
  ]
}
