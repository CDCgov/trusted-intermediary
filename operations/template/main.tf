locals {
  environment_to_rs_environment_prefix_mapping = {
    dev  = "staging"
    stg  = "staging"
    prod = ""
  }
  selected_rs_environment_prefix = lookup(local.environment_to_rs_environment_prefix_mapping, var.environment, "staging")
  rs_domain_prefix               = "${local.selected_rs_environment_prefix}${length(local.selected_rs_environment_prefix) == 0 ? "" : "."}"
  higher_environment_level       = var.environment == "stg" || var.environment == "prd"
}

data "azurerm_resource_group" "group" {
  name = "csels-rsti-${var.environment}-moderate-rg"
}

data "azurerm_client_config" "current" {}
