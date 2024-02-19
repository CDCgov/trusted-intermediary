#resource "azurerm_network_security_group" "example" {
#  name                = "example-security-group"
#  location            = azurerm_resource_group.example.location
#  resource_group_name = azurerm_resource_group.example.name
#}

#data "azurerm_virtual_network" "db_vnet" {
#  name                = "csels-rsti-${var.environment}-moderate-db-vnet"
#  resource_group_name = data.azurerm_resource_group.group.name
#}

data "azurerm_virtual_network" "app" {
  name                = "csels-rsti-${var.environment}-moderate-app-vnet"
  resource_group_name = data.azurerm_resource_group.group.name
}

resource "azurerm_subnet" "app" {
  name                 = "app"
  resource_group_name  = data.azurerm_resource_group.group.name
  virtual_network_name = data.azurerm_virtual_network.app.name
  address_prefixes     = ["172.17.67.128/26"]

  service_endpoints = ["Microsoft.ContainerRegistry", "Microsoft.KeyVault", "Microsoft.Storage", "Microsoft.Web"]

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
  address_prefixes     = ["172.17.67.192/27"]

  delegation {
    name = "delegation"

    service_delegation {
      name    = "Microsoft.DBforPostgreSQL/flexibleServers"
      actions = ["Microsoft.Network/virtualNetworks/subnets/join/action"]
    }
  }
}

data "azurerm_route_table" "route_table" {
  name                = "csels-rsti-${var.environment}-moderate-rt"
  resource_group_name = data.azurerm_resource_group.group.name
}

resource "azurerm_subnet_route_table_association" "app_route_table" {
  subnet_id      = azurerm_subnet.app.id
  route_table_id = data.azurerm_route_table.route_table.id
}

resource "azurerm_subnet_route_table_association" "database_route_table" {
  subnet_id      = azurerm_subnet.database.id
  route_table_id = data.azurerm_route_table.route_table.id
}

data "azurerm_network_security_group" "security_group" {
  name                = "csels-rsti-${var.environment}-moderate-default-sg"
  resource_group_name = data.azurerm_resource_group.group.name
}

resource "azurerm_subnet_network_security_group_association" "app_security_group" {
  subnet_id                 = azurerm_subnet.app.id
  network_security_group_id = data.azurerm_network_security_group.security_group.id
}

resource "azurerm_subnet_network_security_group_association" "database_security_group" {
  subnet_id                 = azurerm_subnet.database.id
  network_security_group_id = data.azurerm_network_security_group.security_group.id
}

#data "azurerm_subnet" "db_subnet" {
#  name                 = data.azurerm_virtual_network.db_vnet.subnets[0]
#  virtual_network_name = data.azurerm_virtual_network.db_vnet.name
#  resource_group_name  = data.azurerm_resource_group.group.name
#}
#
#data "azurerm_subnet" "app_subnet" {
#  name                 = data.azurerm_virtual_network.app_vnet.subnets[0]
#  virtual_network_name = data.azurerm_virtual_network.app_vnet.name
#  resource_group_name  = data.azurerm_resource_group.group.name
#}

resource "azurerm_private_dns_zone" "dns_zone" {
  name                = "privateintermediary.postgres.database.azure.com"
  resource_group_name = data.azurerm_resource_group.group.name
}

resource "azurerm_private_dns_zone_virtual_network_link" "db_network_link" {
  name                  = "intermediarylink.com"
  private_dns_zone_name = azurerm_private_dns_zone.dns_zone.name
  virtual_network_id    = data.azurerm_virtual_network.app.id
  resource_group_name   = data.azurerm_resource_group.group.name
  depends_on            = [azurerm_subnet.database]
}
