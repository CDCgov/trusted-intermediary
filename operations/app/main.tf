terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=3.36.0"
    }
  }

  # Use a remote Terraform state in Azure Storage
  backend "azurerm" {
    resource_group_name  = "cdcti-terraform"
    storage_account_name = "cdctiterraform"
    container_name       = "tfstate"
    key                  = "staging.terraform.tfstate"
  }
}

# Configure the Microsoft Azure Provider
provider "azurerm" {
  use_oidc = true
  features {}
}

# Create the staging resource group
resource "azurerm_resource_group" "staging_group" {
  name     = "cdcti-staging"
  location = "Central US"
}

# Create the container registry
resource "azurerm_container_registry" "staging_registry" {
  name                = "cdctistagingcontainerregistry"
  resource_group_name = azurerm_resource_group.staging_group.name
  location            = azurerm_resource_group.staging_group.location
  sku                 = "Standard"
  admin_enabled       = true
}

# Create the staging service plan
resource "azurerm_service_plan" "staging_plan" {
  name                = "cdcti-staging-service-plan"
  resource_group_name = azurerm_resource_group.staging_group.name
  location            = azurerm_resource_group.staging_group.location
  os_type             = "Linux"
  sku_name            = "B1"
}

# Create the staging App Service
resource "azurerm_linux_web_app" "cdcti-staging-api" {
  name                = "cdcti-staging-api"
  resource_group_name = azurerm_resource_group.staging_group.name
  location            = azurerm_service_plan.staging_plan.location
  service_plan_id     = azurerm_service_plan.staging_plan.id

  site_config {}
}

# Output Container Registry information

output "registry" {
  value = azurerm_container_registry.staging_registry.login_server
}
output "acr_username" {
  value     = azurerm_container_registry.staging_registry.admin_username
  sensitive = true
}
output "acr_password" {
  value     = azurerm_container_registry.staging_registry.admin_password
  sensitive = true
}

# Output App Service information
output "publish_app" {
  value = azurerm_linux_web_app.cdcti-staging-api.name
}
