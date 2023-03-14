locals {
  environment_to_rs_environment_prefix_mapping = {
    dev = "staging"
    staging = "staging"
    prod = ""
  }
  rs_domain_prefix = "${local.environment_to_rs_environment_prefix_mapping[var.environment]}${length(local.environment_to_rs_environment_prefix_mapping[var.environment]) == 0 ? "" : "."}"
}

# Create the staging resource group
resource "azurerm_resource_group" "group" {
  name     = "cdcti-${var.environment}"
  location = "Central US"
}

# Create the container registry
resource "azurerm_container_registry" "registry" {
  name                = "cdcti${var.environment}containerregistry"
  resource_group_name = azurerm_resource_group.group.name
  location            = azurerm_resource_group.group.location
  sku                 = "Standard"
  admin_enabled       = true
}

# Create the staging service plan
resource "azurerm_service_plan" "plan" {
  name                = "cdcti-${var.environment}-service-plan"
  resource_group_name = azurerm_resource_group.group.name
  location            = azurerm_resource_group.group.location
  os_type             = "Linux"
  sku_name            = "B1"
}

# Create the staging App Service
resource "azurerm_linux_web_app" "api" {
  name                = "cdcti-${var.environment}-api"
  resource_group_name = azurerm_resource_group.group.name
  location            = azurerm_service_plan.plan.location
  service_plan_id     = azurerm_service_plan.plan.id

  site_config {}

  app_settings = {
    DOCKER_REGISTRY_SERVER_URL      = "https://${azurerm_container_registry.registry.login_server}"
    DOCKER_REGISTRY_SERVER_USERNAME = azurerm_container_registry.registry.admin_username
    DOCKER_REGISTRY_SERVER_PASSWORD = azurerm_container_registry.registry.admin_password
    REPORT_STREAM_URL_PREFIX = "https://${local.rs_domain_prefix}prime.cdc.gov"
  }

  identity {
    type = "SystemAssigned"
  }
}
