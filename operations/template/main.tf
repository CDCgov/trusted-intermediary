locals {
  environment_to_rs_environment_prefix_mapping = {
    dev     = "staging"
    staging = "staging"
    prod    = ""
  }
  selected_rs_environment_prefix = lookup(local.environment_to_rs_environment_prefix_mapping, var.environment, "staging")
  rs_domain_prefix               = "${local.selected_rs_environment_prefix}${length(local.selected_rs_environment_prefix) == 0 ? "" : "."}"
}

data "azurerm_resource_group" "group" {
  name = "csels-rsti-${var.environment}-moderate-rg"
}

#resource "azurerm_resource_group" "group" {
#  name     = "cdcti-${var.environment}"
#  location = "Central US"
#}

# Create the container registry
resource "azurerm_container_registry" "registry" {
  name                = "cdcti${var.environment}containerregistry"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  sku                 = "Standard"
  admin_enabled       = true
}

# Create the staging service plan
resource "azurerm_service_plan" "plan" {
  name                = "cdcti-${var.environment}-service-plan"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  os_type             = "Linux"
  sku_name            = "B1"
}

# Create the staging App Service
resource "azurerm_linux_web_app" "api" {
  name                = "cdcti-${var.environment}-api2"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = azurerm_service_plan.plan.location
  service_plan_id     = azurerm_service_plan.plan.id

  https_only = true

  site_config {}

  app_settings = {
    DOCKER_REGISTRY_SERVER_URL      = "https://${azurerm_container_registry.registry.login_server}"
    DOCKER_REGISTRY_SERVER_USERNAME = azurerm_container_registry.registry.admin_username
    DOCKER_REGISTRY_SERVER_PASSWORD = azurerm_container_registry.registry.admin_password
    ENV                             = var.environment
    REPORT_STREAM_URL_PREFIX        = "https://${local.rs_domain_prefix}prime.cdc.gov"
    KEY_VAULT_NAME                  = azurerm_key_vault.key_storage.name

  }

  identity {
    type = "SystemAssigned"
  }
}

resource "azurerm_storage_account" "docs" {
  name                            = "cdcti${var.environment}docs2"
  resource_group_name             = data.azurerm_resource_group.group.name
  location                        = data.azurerm_resource_group.group.location
  account_tier                    = "Standard"
  account_replication_type        = "GRS"
  account_kind                    = "StorageV2"
  allow_nested_items_to_be_public = false

  static_website {
    index_document = "index.html"
  }
}
