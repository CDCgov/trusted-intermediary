# Create the container registry
resource "azurerm_container_registry" "registry" {
  name                = "cdcti${var.environment}containerregistry"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  sku                 = "Premium"

  identity {
    type = "UserAssigned"
    identity_ids = [
      azurerm_user_assigned_identity.key_vault_identity.id
    ]
  }

  encryption {
    key_vault_key_id   = azurerm_key_vault_key.customer_managed_key.id
    identity_client_id = azurerm_user_assigned_identity.key_vault_identity.client_id
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

  depends_on = [azurerm_key_vault_access_policy.allow_container_registry_wrapping] // Wait for keyvault access policy to be in place before creating
}

resource "azurerm_user_assigned_identity" "key_vault_identity" {
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location

  name = "key-vault-identity-${var.environment}"

  lifecycle {
    ignore_changes = [
      # below tags are managed by CDC
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

resource "azurerm_role_assignment" "allow_app_to_pull_from_registry" {
  principal_id         = azurerm_linux_web_app.api.identity.0.principal_id
  role_definition_name = "AcrPull"
  scope                = azurerm_container_registry.registry.id
}

resource "azurerm_role_assignment" "allow_app_slot_to_pull_from_registry" {
  principal_id         = azurerm_linux_web_app_slot.pre_live.identity.0.principal_id
  role_definition_name = "AcrPull"
  scope                = azurerm_container_registry.registry.id
}

# Create the staging service plan
resource "azurerm_service_plan" "plan" {
  name                   = "cdcti-${var.environment}-service-plan"
  resource_group_name    = data.azurerm_resource_group.group.name
  location               = data.azurerm_resource_group.group.location
  os_type                = "Linux"
  sku_name               = local.higher_environment_level ? "P1v3" : "P0v3"
  zone_balancing_enabled = local.higher_environment_level

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

# Create the staging App Service
resource "azurerm_linux_web_app" "api" {
  name                = "cdcti-${var.environment}-api"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = azurerm_service_plan.plan.location
  service_plan_id     = azurerm_service_plan.plan.id

  https_only = true

  virtual_network_subnet_id = local.cdc_domain_environment ? azurerm_subnet.app.id : null

  site_config {
    health_check_path                 = "/health"
    health_check_eviction_time_in_min = 5

    scm_use_main_ip_restriction = local.cdc_domain_environment ? true : null

    container_registry_use_managed_identity = true

    application_stack {
      docker_registry_url = "https://${azurerm_container_registry.registry.login_server}"
      docker_image_name   = "ignore_because_specified_later_in_deployment"
    }

    dynamic "ip_restriction" {
      for_each = local.cdc_domain_environment ? [1] : []

      content {
        name       = "deny_all_ipv4"
        action     = "Deny"
        ip_address = "0.0.0.0/0"
        priority   = "200"
      }
    }

    dynamic "ip_restriction" {
      for_each = local.cdc_domain_environment ? [1] : []

      content {
        name       = "deny_all_ipv6"
        action     = "Deny"
        ip_address = "::/0"
        priority   = "201"
      }
    }
  }

  # New settings here should also be added to the pre-live slot's app_settings
  app_settings = {
    ENV                           = var.environment
    REPORT_STREAM_URL_PREFIX      = "https://${local.rs_domain_prefix}prime.cdc.gov"
    KEY_VAULT_NAME                = azurerm_key_vault.key_storage.name
    STORAGE_ACCOUNT_BLOB_ENDPOINT = azurerm_storage_account.storage.primary_blob_endpoint
    METADATA_CONTAINER_NAME       = azurerm_storage_container.metadata.name
    DB_URL                        = azurerm_postgresql_flexible_server.database.fqdn
    DB_PORT                       = "5432"
    DB_NAME                       = "postgres"
    DB_USER                       = "cdcti-${var.environment}-api"
    DB_SSL                        = "require"
    DB_MAX_LIFETIME               = "3480000" # 58 minutes
  }

  identity {
    type = "SystemAssigned"
  }

  lifecycle {
    ignore_changes = [
      site_config[0].application_stack[0].docker_image_name,
      # below tags are managed by CDC
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

resource "azurerm_linux_web_app_slot" "pre_live" {
  name           = "pre-live"
  app_service_id = azurerm_linux_web_app.api.id

  https_only = true

  virtual_network_subnet_id = local.cdc_domain_environment ? azurerm_subnet.app.id : null

  site_config {
    health_check_path                 = "/health"
    health_check_eviction_time_in_min = 5

    scm_use_main_ip_restriction = local.cdc_domain_environment ? true : null

    container_registry_use_managed_identity = true

    application_stack {
      docker_registry_url = "https://${azurerm_container_registry.registry.login_server}"
      docker_image_name   = "ignore_because_specified_later_in_deployment"
    }

    dynamic "ip_restriction" {
      for_each = local.cdc_domain_environment ? [1] : []

      content {
        name       = "deny_all_ipv4"
        action     = "Deny"
        ip_address = "0.0.0.0/0"
        priority   = "200"
      }
    }

    dynamic "ip_restriction" {
      for_each = local.cdc_domain_environment ? [1] : []

      content {
        name       = "deny_all_ipv6"
        action     = "Deny"
        ip_address = "::/0"
        priority   = "201"
      }
    }
  }

  app_settings = {
    ENV                           = var.environment
    REPORT_STREAM_URL_PREFIX      = "https://${local.rs_domain_prefix}prime.cdc.gov"
    KEY_VAULT_NAME                = azurerm_key_vault.key_storage.name
    STORAGE_ACCOUNT_BLOB_ENDPOINT = azurerm_storage_account.storage.primary_blob_endpoint
    METADATA_CONTAINER_NAME       = azurerm_storage_container.metadata.name
    DB_URL                        = azurerm_postgresql_flexible_server.database.fqdn
    DB_PORT                       = "5432"
    DB_NAME                       = "postgres"
    DB_USER                       = "cdcti-${var.environment}-api"
    DB_SSL                        = "require"
    DB_MAX_LIFETIME               = "3480000" # 58 minutes
  }

  identity {
    type = "SystemAssigned"
  }

  lifecycle {
    ignore_changes = [
      site_config[0].application_stack[0].docker_image_name,
      # Ignore changes to tags because the CDC sets these automagically
      tags,
    ]
  }
}

resource "azurerm_monitor_autoscale_setting" "api_autoscale" {
  name                = "api_autoscale"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  target_resource_id  = azurerm_service_plan.plan.id

  profile {
    name = "defaultProfile"

    capacity {
      default = local.higher_environment_level ? 3 : 1
      minimum = local.higher_environment_level ? 3 : 1
      maximum = local.higher_environment_level ? 10 : 1
    }

    rule {
      metric_trigger {
        metric_name        = "CpuPercentage"
        metric_resource_id = azurerm_service_plan.plan.id
        time_grain         = "PT1M"
        statistic          = "Average"
        time_window        = "PT5M"
        time_aggregation   = "Average"
        operator           = "GreaterThan"
        threshold          = 75
        metric_namespace   = "microsoft.web/serverfarms"
      }

      scale_action {
        direction = "Increase"
        type      = "ChangeCount"
        value     = "1"
        cooldown  = "PT1M"
      }
    }

    rule {
      metric_trigger {
        metric_name        = "CpuPercentage"
        metric_resource_id = azurerm_service_plan.plan.id
        time_grain         = "PT1M"
        statistic          = "Average"
        time_window        = "PT5M"
        time_aggregation   = "Average"
        operator           = "LessThan"
        threshold          = 25
        metric_namespace   = "microsoft.web/serverfarms"
      }

      scale_action {
        direction = "Decrease"
        type      = "ChangeCount"
        value     = "1"
        cooldown  = "PT5M"
      }
    }
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
