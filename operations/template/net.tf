#resource "azurerm_network_security_group" "example" {
#  name                = "example-security-group"
#  location            = azurerm_resource_group.example.location
#  resource_group_name = azurerm_resource_group.example.name
#}

data "azurerm_virtual_network" "db_vnet" {
  name                = "csels-rsti-${var.environment}-moderate-db-vnet"
  resource_group_name = data.azurerm_resource_group.group.name
}

data "azurerm_virtual_network" "app_vnet" {
  name                = "csels-rsti-${var.environment}-moderate-app-vnet"
  resource_group_name = data.azurerm_resource_group.group.name
}

data "azurerm_subnet" "db_subnet" {
  name                 = data.azurerm_virtual_network.db_vnet.subnets[0]
  virtual_network_name = data.azurerm_virtual_network.db_vnet.name
  resource_group_name  = data.azurerm_resource_group.group.name
}

data "azurerm_subnet" "app_subnet" {
  name                 = data.azurerm_virtual_network.app_vnet.subnets[0]
  virtual_network_name = data.azurerm_virtual_network.app_vnet.name
  resource_group_name  = data.azurerm_resource_group.group.name
}

resource "azurerm_private_dns_zone" "dns_zone" {
  name                = "privateintermediary.postgres.database.azure.com"
  resource_group_name = data.azurerm_resource_group.group.name
}

resource "azurerm_private_dns_zone_virtual_network_link" "db_network_link" {
  name                  = "intermediarylink.com"
  private_dns_zone_name = azurerm_private_dns_zone.dns_zone.name
  virtual_network_id    = data.azurerm_virtual_network.app_vnet.id
  resource_group_name   = data.azurerm_resource_group.group.name
  depends_on            = [data.azurerm_subnet.db_subnet]
}
