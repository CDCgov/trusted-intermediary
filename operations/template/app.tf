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
  sku_name            = "P1v3"
  worker_count        = 3
}

# Create the staging App Service
resource "azurerm_linux_web_app" "api" {
  name                = "cdcti-${var.environment}-api"
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
    STORAGE_ACCOUNT_BLOB_ENDPOINT   = azurerm_storage_account.storage.primary_blob_endpoint
    METADATA_CONTAINER_NAME         = azurerm_storage_container.metadata.name
    DB_URL                          = azurerm_postgresql_flexible_server.database.fqdn
    DB_PORT                         = "5432"
    DB_NAME                         = "postgres"
    DB_USER                         = "cdcti-${var.environment}-api"
    DB_SSL                          = "require"
  }

  identity {
    type = "SystemAssigned"
  }
}
