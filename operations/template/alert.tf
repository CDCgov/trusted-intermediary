resource "azurerm_monitor_action_group" "monitor" {
  name                = "cdcti${var.environment}-actiongroup"
  resource_group_name = data.azurerm_resource_group.group.name
  short_name          = "cdcti-alerts"

  email_receiver {
    name          = "cdcti-flexion-slack-email-receiver"
    email_address = var.alert_slack_email
  }
}

resource "azurerm_monitor_metric_alert" "alert" {
  name                = "db-connection-metric-alert"
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [azurerm_postgresql_flexible_server.database.id]
  description         = "Action will be triggered when database connection failure count is 1 or more each minute for the passed minute"
  frequency        = "PT1M"
  window_size      = "PT1M"

  criteria {
    metric_namespace = "Microsoft.DBforPostgreSQL/flexibleServers"
    metric_name      = "connections_failed"
    aggregation      = "Total"
    operator         = "GreaterThan"
    threshold        = 0
  }

  action {
    action_group_id = azurerm_monitor_action_group.monitor.id
  }
}

resource "azurerm_monitor_scheduled_query_rules_alert" "example" {
  name                = "cdcti-${var.environment}-api-log-token-alert"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  action {
  action_group           = [azurerm_monitor_action_group.monitor.id]
  email_subject          = "FATAL: The access token has expired!"
  }

  data_source_id = azurerm_linux_web_app.api.id
  description    = "Alert when total results cross threshold"
  enabled        = true

  query       = <<-QUERY
      AppServiceConsoleLogs
      | where ResultDescription has "FATAL: The access token has expired."
      and TimeGenerated >= ago(30m)
      and TimeGenerated <= now()
      | summarize count()
    QUERY

  severity    = 3
  frequency   = 10
  time_window = 30

  trigger {
    operator  = "GreaterThan"
    threshold = 5
  }
}
