resource "azurerm_monitor_action_group" "notify_slack_email" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti${var.environment}-actiongroup"
  resource_group_name = data.azurerm_resource_group.group.name
  short_name          = "cdcti-alerts"

  email_receiver {
    name          = "cdcti-flexion-slack-email-receiver"
    email_address = var.alert_slack_email
  }

  # Ignore changes to tags because the CDC sets these automagically
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

resource "azurerm_monitor_activity_log_alert" "azure_service_health_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-azure-status-alert"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = ["/subscriptions/${data.azurerm_client_config.current.subscription_id}"]

  criteria {
    category = "ServiceHealth"
    levels   = ["Error"]
    service_health {
      locations = ["global"]
      events    = ["Incident"]
    }
  }

  action {
    action_group_id = azurerm_monitor_action_group.notify_slack_email[count.index].id
  }

  description = "Alert service(s) appear to be down"
  enabled     = true

  # Ignore changes to tags because the CDC sets these automagically
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

resource "azurerm_monitor_scheduled_query_rules_alert" "database_token_expired_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-api-log-token-alert"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  action {
    action_group  = [azurerm_monitor_action_group.notify_slack_email[count.index].id]
    email_subject = "FATAL: The access token has expired!"
  }

  data_source_id = azurerm_linux_web_app.api.id
  description    = "Alert when total results cross threshold"
  enabled        = true

  query = <<-QUERY
      AppServiceConsoleLogs
      | where ResultDescription has "FATAL: The access token has expired."
      and TimeGenerated >= ago(30m)
      and TimeGenerated <= now()
      | summarize count()
    QUERY

  severity                = 3
  frequency               = 10
  time_window             = 30
  auto_mitigation_enabled = true

  trigger {
    operator  = "GreaterThan"
    threshold = 1
  }

  lifecycle {
    # Ignore changes to tags because the CDC sets these automagically
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

resource "azurerm_monitor_metric_alert" "azure_4XX_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-azure-http-4XX-alert"
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [data.azurerm_resource_group.group.id]
  description         = "Action will be triggered when Http Status Code 4XX is greater than or equal to 3"
  frequency           = "PT1M" // Checks every 1 minute
  window_size         = "PT1H" // Every Check looks back 1 hour for 4xx errors

  criteria {
    metric_namespace = "Microsoft.Web/sites"
    metric_name      = "Http4xx"
    aggregation      = "Count"
    operator         = "GreaterThanOrEqual"
    threshold        = 3
  }

  action {
    action_group_id = azurerm_monitor_action_group.notify_slack_email[count.index].id
  }

  lifecycle {
    # Ignore changes to tags because the CDC sets these automagically
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
