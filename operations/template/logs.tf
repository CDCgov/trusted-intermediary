resource "azurerm_log_analytics_workspace" "logs_workspace" {
  name = "ti-logs-${var.environment}"

  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location

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

resource "azurerm_log_analytics_query_pack" "application_logs_pack" {
  name                = "TI Application Logs"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location

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

resource "azurerm_log_analytics_query_pack_query" "live_application_logs" {
  display_name = "TI's Live Slot Raw Application Logs"
  description  = "View all TI's live slot application logs in a structured format"

  query_pack_id = azurerm_log_analytics_query_pack.application_logs_pack.id
  categories    = ["applications"]

  body = "AppServiceConsoleLogs | where _ResourceId !contains 'pre-live' | project JsonResult = parse_json(ResultDescription) | evaluate bag_unpack(JsonResult) | project-reorder ['@timestamp'], level, message"
}

resource "azurerm_log_analytics_query_pack_query" "prelive_application_logs" {
  display_name = "TI's Pre-Live Slot Raw Application Logs"
  description  = "View all TI's pre-live slot application logs in a structured format"

  query_pack_id = azurerm_log_analytics_query_pack.application_logs_pack.id
  categories    = ["applications"]

  body = "AppServiceConsoleLogs | where _ResourceId contains 'pre-live' | project JsonResult = parse_json(ResultDescription) | evaluate bag_unpack(JsonResult) | project-reorder ['@timestamp'], level, message"
}

resource "azurerm_log_analytics_query_pack_query" "application_error_logs" {
  display_name = "TI's Application Error Logs"
  description  = "View all TI's application logs with error level in a structured format"

  query_pack_id = azurerm_log_analytics_query_pack.application_logs_pack.id
  categories    = ["applications"]

  body = "AppServiceConsoleLogs | project JsonResult = parse_json(ResultDescription) | evaluate bag_unpack(JsonResult) | where level == 'ERROR' | project-away level | project-reorder ['@timestamp'], message"
}

resource "azurerm_monitor_diagnostic_setting" "app_to_logs" {
  name                       = "ti-app-to-logs-${var.environment}"
  target_resource_id         = azurerm_linux_web_app.api.id
  log_analytics_workspace_id = azurerm_log_analytics_workspace.logs_workspace.id

  log_analytics_destination_type = "Dedicated"

  enabled_log {
    category = "AppServiceConsoleLogs"
  }
  enabled_log {
    category = "AppServiceAppLogs"
  }
  enabled_log {
    category = "AppServiceHTTPLogs"
  }
  enabled_log {
    category = "AppServicePlatformLogs"
  }
}

resource "azurerm_monitor_diagnostic_setting" "prelive_slot_to_logs" {
  name                       = "ti-prelive-slot-to-logs-${var.environment}"
  target_resource_id         = azurerm_linux_web_app_slot.pre_live.id
  log_analytics_workspace_id = azurerm_log_analytics_workspace.logs_workspace.id

  log_analytics_destination_type = "Dedicated"

  enabled_log {
    category = "AppServiceConsoleLogs"
  }
  enabled_log {
    category = "AppServiceAppLogs"
  }
  enabled_log {
    category = "AppServiceHTTPLogs"
  }
  enabled_log {
    category = "AppServicePlatformLogs"
  }
}
