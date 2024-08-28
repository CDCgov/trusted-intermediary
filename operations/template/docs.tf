resource "azurerm_storage_account" "docs" {
  name                              = "cdcti${var.environment}docs"
  resource_group_name               = data.azurerm_resource_group.group.name
  location                          = data.azurerm_resource_group.group.location
  account_tier                      = "Standard"
  account_replication_type          = "GRS"
  account_kind                      = "StorageV2"
  allow_nested_items_to_be_public   = false
  min_tls_version                   = "TLS1_2"
  infrastructure_encryption_enabled = true

  static_website {
    index_document = "index.html"
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
      customer_managed_key,
    ]
  }

  identity {
    type = "SystemAssigned"
  }
}


resource "azurerm_key_vault_managed_storage_account" "key_vault_storage_account" {
  name                         = "key-vault-storage-account-${var.environment}"
  key_vault_id                 = azurerm_key_vault.key_storage.id
  storage_account_id           = azurerm_storage_account.docs.id
  storage_account_key          = "key1" # pragma: allowlist secret
#   regenerate_key_automatically = false
#   regeneration_period          = "P360D"
}
