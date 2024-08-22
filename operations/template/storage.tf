
resource "azurerm_key_vault" "keyVault" {
  name                = "tiStorageKeyVault"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"

  purge_protection_enabled = true
}

resource "azurerm_key_vault_access_policy" "accessPolicyStorage" {
  key_vault_id = azurerm_key_vault.keyVault.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = azurerm_storage_account.storage.identity[0].principal_id

  secret_permissions = ["Get"]
  key_permissions = [
    "Get",
    "UnwrapKey",
    "WrapKey"
  ]
}

resource "azurerm_key_vault_access_policy" "accessPolicyClient" {
  key_vault_id = azurerm_key_vault.keyVault.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = data.azurerm_client_config.current.object_id

  secret_permissions = ["Get"]
  key_permissions = [
    "Get",
    "Create",
    "Delete",
    "List",
    "Restore",
    "Recover",
    "UnwrapKey",
    "WrapKey",
    "Purge",
    "Encrypt",
    "Decrypt",
    "Sign",
    "Verify",
    "GetRotationPolicy",
    "SetRotationPolicy"
  ]
}


resource "azurerm_key_vault_key" "keyVaultKey" {
  name         = "vault-key"
  key_vault_id = azurerm_key_vault.keyVault.id
  key_type     = "RSA"
  key_size     = 2048
  key_opts = [
    "decrypt",
    "encrypt",
    "sign",
    "unwrapKey",
    "verify",
    "wrapKey"
  ]

  depends_on = [
    azurerm_key_vault_access_policy.accessPolicyStorage,
    azurerm_key_vault_access_policy.accessPolicyClient
  ]
}

resource "azurerm_storage_account" "storage" {
  name                              = "cdcti${var.environment}"
  resource_group_name               = data.azurerm_resource_group.group.name
  location                          = data.azurerm_resource_group.group.location
  account_tier                      = "Standard"
  account_replication_type          = "GRS"
  account_kind                      = "StorageV2"
  allow_nested_items_to_be_public   = false
  min_tls_version                   = "TLS1_2"
  infrastructure_encryption_enabled = true

  identity {
    type = "SystemAssigned"
  }

  #   below tags are managed by CDC
  lifecycle {
    ignore_changes = [
      tags["business_steward"],
      tags["center"],
      tags["environment"],
      tags["escid"],
      tags["funding_source"],
      tags["pii_data"],
      tags["security_compliance"],
      tags["security_steward"],
      tags["support_group"],
      tags["system"],
      tags["technical_steward"],
      tags["zone"],
      customer_managed_key
    ]
  }
}

resource "azurerm_storage_container" "metadata" {
  name                  = "metadata"
  storage_account_name  = azurerm_storage_account.storage.name
  container_access_type = "private"
}

resource "azurerm_role_assignment" "allow_api_read_write" {
  scope                = azurerm_storage_container.metadata.resource_manager_id
  role_definition_name = "Storage Blob Data Contributor"
  principal_id         = azurerm_linux_web_app.api.identity.0.principal_id
}

resource "azurerm_storage_account_customer_managed_key" "customerKey" {
  storage_account_id = azurerm_storage_account.storage.id
  key_vault_id       = azurerm_key_vault.keyVault.id
  key_name           = azurerm_key_vault_key.keyVaultKey.name
}
