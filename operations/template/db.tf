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

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "admin_for_deployer" {
  server_name         = azurerm_postgresql_flexible_server.database.name
  resource_group_name = data.azurerm_resource_group.group.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = var.deployer_id
  principal_name      = "cdcti-github"
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


resource "azurerm_postgresql_flexible_server_firewall_rule" "db_firewall_1" {
  name                = "AllowPeter"
  server_id           = azurerm_postgresql_flexible_server.database.id
  start_ip_address    = "45.16.213.131"
  end_ip_address      = "45.16.213.131"
}


resource "azurerm_postgresql_flexible_server_firewall_rule" "db_firewall_2" {
  name                = "AllowJeff"
  server_id           = azurerm_postgresql_flexible_server.database.id
  start_ip_address    = "50.5.94.248"
  end_ip_address      = "50.5.94.248"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "db_firewall_3" {
  name                = "AllowTiff"
  server_id           = azurerm_postgresql_flexible_server.database.id
  start_ip_address    = "107.15.235.75"
  end_ip_address      = "107.15.235.75"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "db_firewall_4" {
  name                = "AllowJorge"
  server_id           = azurerm_postgresql_flexible_server.database.id
  start_ip_address    = "107.201.135.214"
  end_ip_address      = "107.201.135.214"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "db_firewall_5" {
  name                = "AllowAzure"
  server_id           = azurerm_postgresql_flexible_server.database.id
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "0.0.0.0"
}