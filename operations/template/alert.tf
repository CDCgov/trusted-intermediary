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
  location            = "global"
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
  scopes              = [azurerm_linux_web_app.api.id]
  description         = "Action will be triggered when Http Status Code 4XX is greater than or equal to 3"
  frequency           = "PT1M" // Checks every 1 minute
  window_size         = "PT1H" // Every Check looks back 1 hour for 4xx errors

  criteria {
    metric_namespace = "Microsoft.Web/sites"
    metric_name      = "Http4xx"
    aggregation      = "Total"
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

resource "azurerm_monitor_metric_alert" "azure_5XX_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-azure-http-5XX-alert"
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [azurerm_linux_web_app.api.id]
  description         = "Action will be triggered when Http Status Code 5XX is greater than or equal to 1"
  frequency           = "PT1M" // Checks every 1 min
  window_size         = "PT5M" // Every Check looks back 5 min for 5xx errors

  criteria {
    metric_namespace = "Microsoft.Web/sites"
    metric_name      = "Http5xx"
    aggregation      = "Total"
    operator         = "GreaterThanOrEqual"
    threshold        = 1
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
    threshold        = azurerm_monitor_autoscale_setting.api_autoscale.profile[0].capacity[0].default - 0.5
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

resource "azurerm_monitor_scheduled_query_rules_alert" "ti-log-errors-alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-log-errors-alert"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  action {
    action_group  = [azurerm_monitor_action_group.notify_slack_email[count.index].id]
    email_subject = "${var.environment}: TI log errors detected!"
  }

  data_source_id = azurerm_linux_web_app.api.id
  description    = "Alert when total errors cross threshold"
  enabled        = true

  query = <<-QUERY
      AppServiceConsoleLogs
      | where TimeGenerated >= ago(00.001m)
      and TimeGenerated <= now()
      | project columnifexists("ResultDescription", 'default_value')
      | project  JsonResult = parse_json(ResultDescription)
      | evaluate bag_unpack(JsonResult) : (level:string)
      | where level in ( 'ERROR' )
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
