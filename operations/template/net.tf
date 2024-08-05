data "azurerm_virtual_network" "app" {
  name                = "csels-rsti-${var.environment}-moderate-app-vnet"
  resource_group_name = data.azurerm_resource_group.group.name
}

locals {
  subnets_cidrs = cidrsubnets(data.azurerm_virtual_network.app.address_space[0], 2, 2, 2, 3, 3)
}

resource "azurerm_subnet" "app" {
  name                 = "app"
  resource_group_name  = data.azurerm_resource_group.group.name
  virtual_network_name = data.azurerm_virtual_network.app.name
  address_prefixes     = [local.subnets_cidrs[0]]

  service_endpoints = [
    "Microsoft.AzureActiveDirectory",
    "Microsoft.AzureCosmosDB",
    "Microsoft.ContainerRegistry",
    "Microsoft.EventHub",
    "Microsoft.KeyVault",
    "Microsoft.ServiceBus",
    "Microsoft.Sql",
    "Microsoft.Storage",
    "Microsoft.Web",
  ]

  delegation {
    name = "delegation"

    service_delegation {
      name    = "Microsoft.Web/serverFarms"
      actions = ["Microsoft.Network/virtualNetworks/subnets/join/action"]
    }
  }
}

resource "azurerm_subnet" "database" {
  name                 = "database"
  resource_group_name  = data.azurerm_resource_group.group.name
  virtual_network_name = data.azurerm_virtual_network.app.name
  address_prefixes     = [local.subnets_cidrs[1]]

  service_endpoints = [
    "Microsoft.AzureActiveDirectory",
    "Microsoft.AzureCosmosDB",
    "Microsoft.ContainerRegistry",
    "Microsoft.EventHub",
    "Microsoft.KeyVault",
    "Microsoft.ServiceBus",
    "Microsoft.Sql",
    "Microsoft.Storage",
    "Microsoft.Web",
  ]

  delegation {
    name = "delegation"

    service_delegation {
      name    = "Microsoft.DBforPostgreSQL/flexibleServers"
      actions = ["Microsoft.Network/virtualNetworks/subnets/join/action", "Microsoft.Network/virtualNetworks/subnets/prepareNetworkPolicies/action"]
    }
  }
}

resource "azurerm_subnet" "vpn" {
  name                 = "GatewaySubnet"
  resource_group_name  = data.azurerm_resource_group.group.name
  virtual_network_name = data.azurerm_virtual_network.app.name
  address_prefixes     = [local.subnets_cidrs[2]]
}

resource "azurerm_subnet" "resolver_inbound" {
  name                 = "resolver-inbound"
  resource_group_name  = data.azurerm_resource_group.group.name
  virtual_network_name = data.azurerm_virtual_network.app.name
  address_prefixes     = [local.subnets_cidrs[3]]

  delegation {
    name = "delegation"

    service_delegation {
      name    = "Microsoft.Network/dnsResolvers"
      actions = ["Microsoft.Network/virtualNetworks/subnets/join/action"]
    }
  }
}

resource "azurerm_subnet" "resolver_outbound" {
  name                 = "resolver-outbound"
  resource_group_name  = data.azurerm_resource_group.group.name
  virtual_network_name = data.azurerm_virtual_network.app.name
  address_prefixes     = [local.subnets_cidrs[4]]

  delegation {
    name = "delegation"

    service_delegation {
      name    = "Microsoft.Network/dnsResolvers"
      actions = ["Microsoft.Network/virtualNetworks/subnets/join/action"]
    }
  }
}

resource "azurerm_private_dns_zone" "dns_zone" {
  name                = "privateintermediary.postgres.database.azure.com"
  resource_group_name = data.azurerm_resource_group.group.name

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

resource "azurerm_private_dns_zone_virtual_network_link" "db_network_link" {
  name                  = "db_network_link"
  private_dns_zone_name = azurerm_private_dns_zone.dns_zone.name
  virtual_network_id    = data.azurerm_virtual_network.app.id
  resource_group_name   = data.azurerm_resource_group.group.name
  depends_on            = [azurerm_subnet.database]

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

data "azurerm_network_security_group" "db_security_group" {
  name                = "database-security-group"
  resource_group_name = data.azurerm_resource_group.group.name
}

data "azurerm_route_table" "database" {
  name                = "database-route-table"
  resource_group_name = data.azurerm_resource_group.group.name
}


resource "azurerm_subnet_route_table_association" "database_database" {
  subnet_id      = azurerm_subnet.database.id
  route_table_id = data.azurerm_route_table.database.id
}

resource "azurerm_subnet_network_security_group_association" "database_security_group" {
  subnet_id                 = azurerm_subnet.database.id
  network_security_group_id = data.azurerm_network_security_group.db_security_group.id
}

data "azurerm_network_security_group" "app_security_group" {
  name                = "app-security-group"
  resource_group_name = data.azurerm_resource_group.group.name
}

resource "azurerm_subnet_network_security_group_association" "app_security_group" {
  subnet_id                 = azurerm_subnet.app.id
  network_security_group_id = data.azurerm_network_security_group.app_security_group.id
}
