resource "azurerm_storage_account" "alerts" {
  name                     = "cdcti${var.environment}-alerts"
  resource_group_name      = data.azurerm_resource_group.group.name
  location                 = data.azurerm_resource_group.group.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_monitor_action_group" "monitor" {
  name                = "cdcti${var.environment}-actiongroup"
  resource_group_name = data.azurerm_resource_group.group.name
  short_name          = "cdcti-alerts"

  webhook_receiver {
    name        = "cdcti-flexion-slack-webhook-receiver"
    service_uri = var.alert_slack_webhook
  }
}

resource "azurerm_monitor_metric_alert" "alert" {
  name                = "db-connection-metric-alert"
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [azurerm_postgresql_flexible_server.database.id]
  description         = "Action will be triggered when database connection failure count is 1 or more each minute for the passed minute"

  criteria {
    metric_namespace = "Microsoft.cdcti-${var.environment}-database/flexibleServers"
    metric_name      = "connections_failed"
    aggregation      = "Total"
    operator         = "GreaterThan"
    threshold        = 0
    frequency        = "PT1M"
    window_size      = "PT1M"
  }

  action {
    action_group_id = azurerm_monitor_action_group.monitor.id
  }
}
