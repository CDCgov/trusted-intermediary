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

  app_settings = {
    DOCKER_REGISTRY_SERVER_URL      = "https://${azurerm_container_registry.staging_registry.login_server}"
    DOCKER_REGISTRY_SERVER_USERNAME = azurerm_container_registry.staging_registry.admin_username
    DOCKER_REGISTRY_SERVER_PASSWORD = azurerm_container_registry.staging_registry.admin_password
  }
}
