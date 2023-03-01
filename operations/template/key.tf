resource "azurerm_key_vault" "key_storage" {
  name = "key-vault-${var.environment}"

  resource_group_name = azurerm_resource_group.group.name
  location            = azurerm_resource_group.group.location

  sku_name  = "standard"
  tenant_id = data.azurerm_client_config.current.tenant_id

  purge_protection_enabled = false
}

resource "azurerm_key_vault_secret" "report_stream_sender_private_key" {
  name  = "report-stream-sender-private-key-${var.environment}"
  value = "dogcow"

  key_vault_id = azurerm_key_vault.key_storage.id

  lifecycle {
    ignore_changes = [value]
  }
}

data "azurerm_client_config" "current" {}
