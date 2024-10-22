resource "azurerm_monitor_action_group" "notify_slack_email" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti${var.environment}-actiongroup"
  resource_group_name = data.azurerm_resource_group.group.name
  short_name          = "cdcti-alerts"

  email_receiver {
    name          = "cdcti-flexion-slack-email-receiver"
    email_address = var.alert_slack_email
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
resource "azurerm_monitor_metric_alert" "low_instance_count_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-azure-low-instance-count-alert"
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [azurerm_monitor_autoscale_setting.api_autoscale.id]
  description         = "The instance count in ${var.environment} is too low"
  severity            = 2       // warning
  frequency           = "PT1M"  // Checks every 1 minute
  window_size         = "PT15M" // Every Check, looks back 15 minutes in history

  criteria {
    metric_namespace = "Microsoft.Insights/autoscalesettings"
    metric_name      = "ObservedCapacity"
    aggregation      = "Average"
    operator         = "LessThanOrEqual"
    // This threshold is based on the autoscale settings in app.tf
    // How should we tune these numbers if we've scaled up higher than the initial count of 3/1?
    threshold = local.higher_environment_level ? 2.5 : 0.5
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
