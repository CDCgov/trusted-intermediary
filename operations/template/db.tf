resource "azurerm_postgresql_flexible_server" "database" {
  name                = "cdcti-${var.environment}-database"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  sku_name            = "B_Standard_B1ms"
  version             = "16"
  storage_mb          = "32768"
  backup_retention_days = "14"
  administrator_login = "user"
  administrator_password = "test"
}
