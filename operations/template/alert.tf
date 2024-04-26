resource "azurerm_storage_account" "alerts" {
  name                     = "cdcti${var.environment}alerts"
  resource_group_name      = data.azurerm_resource_group.group.name
  location                 = azurerm_resource_group.group.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_monitor_action_group" "monitor" {
  name                = "cdcti${var.environment}-actiongroup"
  resource_group_name = data.azurerm_resource_group.group.name
  short_name          = "cdcti-alerts"

  webhook_receiver {
    name        = "notsureyet"
    service_uri = "http://our-slack-webhook.com/channel"
  }
}

resource "azurerm_monitor_metric_alert" "example" {
  name                = "db-connection-metric-alert"
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [azurerm_storage_account.alerts.id]
  description         = "Action will be triggered when Transactions count is greater than 50."

  criteria {
    metric_namespace = "Microsoft.Storage/storageAccounts"
    metric_name      = "Transactions"
    aggregation      = "Total"
    operator         = "GreaterThan"
    threshold        = 50

    dimension {
      name     = "ApiName"
      operator = "Include"
      values   = ["*"]
    }
  }

  action {
    action_group_id = azurerm_monitor_action_group.monitor.id
  }
}
