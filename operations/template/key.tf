resource "azurerm_key_vault" "key_storage" {
  name = "ti-key-vault-${var.environment}"

  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location

  sku_name  = "standard"
  tenant_id = data.azurerm_client_config.current.tenant_id

  purge_protection_enabled = true

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
      tags["zone"]
    ]
  }
}

resource "azurerm_key_vault_access_policy" "allow_github_deployer" {
  key_vault_id = azurerm_key_vault.key_storage.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = var.deployer_id

  secret_permissions = [
    "Set",
    "Get",
    "Delete",
    "Purge",
    "Recover",
  ]

  key_permissions = [
    "Create",
    "Delete",
    "Get",
    "Purge",
    "Recover",
    "Update",
    "GetRotationPolicy",
    "SetRotationPolicy",
  ]
}

resource "azurerm_key_vault_access_policy" "allow_api_read" {
  key_vault_id = azurerm_key_vault.key_storage.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = azurerm_linux_web_app.api.identity.0.principal_id

  secret_permissions = [
    "List",
    "Get",
  ]
}

resource "azurerm_key_vault_access_policy" "allow_storage_account_wrapping" {
  key_vault_id = azurerm_key_vault.key_storage.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = azurerm_storage_account.docs.identity.0.principal_id

  key_permissions = [
    "Get",
    "UnwrapKey",
    "WrapKey",
  ]
}

resource "azurerm_key_vault_access_policy" "allow_storage_account_real_wrapping" {
  key_vault_id = azurerm_key_vault.key_storage.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = azurerm_storage_account.storage.identity.0.principal_id

  key_permissions = [
    "Get",
    "UnwrapKey",
    "WrapKey",
  ]
}

resource "azurerm_key_vault_secret" "report_stream_public_key" {
  name  = "organization-report-stream-public-key-${var.environment}"
  value = "dogcow"

  key_vault_id = azurerm_key_vault.key_storage.id

  lifecycle {
    ignore_changes = [value]
  }
  depends_on = [azurerm_key_vault_access_policy.allow_github_deployer] //wait for the permission that allows our deployer to write the secret
}

resource "azurerm_key_vault_secret" "trusted_intermediary_public_key" {
  name  = "organization-trusted-intermediary-public-key-${var.environment}"
  value = "dogcow"

  key_vault_id = azurerm_key_vault.key_storage.id

  lifecycle {
    ignore_changes = [value]
  }
  depends_on = [azurerm_key_vault_access_policy.allow_github_deployer] //wait for the permission that allows our deployer to write the secret
}

resource "azurerm_key_vault_secret" "trusted_intermediary_public_key_internal" {
  name  = "trusted-intermediary-public-key-${var.environment}"
  value = "dogcow"

  key_vault_id = azurerm_key_vault.key_storage.id

  lifecycle {
    ignore_changes = [value]
  }
  depends_on = [azurerm_key_vault_access_policy.allow_github_deployer] //wait for the permission that allows our deployer to write the secret
}

resource "azurerm_key_vault_secret" "trusted_intermediary_private_key" {
  name  = "trusted-intermediary-private-key-${var.environment}"
  value = "dogcow"

  key_vault_id = azurerm_key_vault.key_storage.id

  lifecycle {
    ignore_changes = [value]
  }
  depends_on = [azurerm_key_vault_access_policy.allow_github_deployer] //wait for the permission that allows our deployer to write the secret
}


resource "azurerm_key_vault_key" "customer_managed_key" {
  name         = "customer-managed-key-${var.environment}"
  key_vault_id = azurerm_key_vault.key_storage.id

  key_type = "RSA"
  key_size = 4096

  key_opts = [
    "decrypt",
    "encrypt",
    "sign",
    "unwrapKey",
    "verify",
    "wrapKey"
  ]

  depends_on = [azurerm_key_vault_access_policy.allow_github_deployer] //wait for the permission that allows our deployer to write the secret
}
