resource "azurerm_key_vault" "key_storage" {
  name = "key-vault-${var.environment}"

  resource_group_name = azurerm_resource_group.group.name
  location            = azurerm_resource_group.group.location

  sku_name  = "standard"
  tenant_id = data.azurerm_client_config.current.tenant_id

  purge_protection_enabled = false
}

data "azurerm_client_config" "current" {}
