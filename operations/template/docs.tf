resource "azurerm_storage_account" "docs" {
  name                            = "cdcti${var.environment}docs"
  resource_group_name             = data.azurerm_resource_group.group.name
  location                        = data.azurerm_resource_group.group.location
  account_tier                    = "Standard"
  account_replication_type        = "GRS"
  account_kind                    = "StorageV2"
  allow_nested_items_to_be_public = false

  static_website {
    index_document = "index.html"
  }
}
