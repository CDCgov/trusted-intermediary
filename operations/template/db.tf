data "azuread_service_principal" "principal" {
  object_id = data.azurerm_client_config.current.object_id
}

resource "azurerm_postgresql_flexible_server" "database" {
  name                = "cdcti-${var.environment}-database"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  sku_name            = "B_Standard_B1ms"
  version             = "16"
  storage_mb          = "32768"
  backup_retention_days = "14"

  authentication {
    password_auth_enabled = "false"
    active_directory_auth_enabled = "true"
    tenant_id = data.azurerm_client_config.current.tenant_id
  }

  lifecycle {
    ignore_changes = [
      zone,
      high_availability.0.standby_availability_zone
    ]
  }
}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "entra" {
  server_name         = azurerm_postgresql_flexible_server.database.name
  resource_group_name = data.azurerm_resource_group.group.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = data.azuread_service_principal.principal.object_id
  principal_name      = data.azuread_service_principal.principal.display_name
  principal_type      = "ServicePrincipal"
}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "admin_for_app" {
  server_name         = azurerm_postgresql_flexible_server.database.name
  resource_group_name = data.azurerm_resource_group.group.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = azurerm_linux_web_app.api.identity.0.principal_id
  principal_name      = azurerm_linux_web_app.api.name
  principal_type      = "ServicePrincipal"
}
