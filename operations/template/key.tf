resource "azurerm_key_vault" "key_storage" {
  name = "key-vault-${var.environment}"

  resource_group_name = azurerm_resource_group.group.name
  location            = azurerm_resource_group.group.location

  sku_name  = "standard"
  tenant_id = data.azurerm_client_config.current.tenant_id

  purge_protection_enabled = false

  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = "d59c2c86-de5e-41b7-a752-0869a73f5a60"

    secret_permissions = [
      "Set",
      "Get",
      "Delete",
      "Purge",
    ]
  }
}

resource "azurerm_key_vault_access_policy" "allow_github_deployer" {
  key_vault_id = azurerm_key_vault.key_storage.id
  tenant_id = data.azurerm_client_config.current.tenant_id
  object_id = "d59c2c86-de5e-41b7-a752-0869a73f5a60"  //github app registration

  secret_permissions = [
    "Set",
    "Get",
    "Delete",
    "Purge",
  ]
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
