resource "azurerm_subnet" "vpn" {
  name                 = "GatewaySubnet"
  resource_group_name  = data.azurerm_resource_group.group.name
  virtual_network_name = data.azurerm_virtual_network.app.name
  address_prefixes     = ["172.17.68.0/26"]
}

resource "azurerm_public_ip" "vpn" {
  name                = "vpn-public-ip"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  allocation_method = "Dynamic"
}

resource "azurerm_virtual_network_gateway" "vpn" {
  name                = "${var.environment}-vpn"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  type     = "Vpn"
  vpn_type = "RouteBased"

  active_active = false
  enable_bgp    = false
  sku           = "VpnGw1"

  ip_configuration {
    public_ip_address_id          = azurerm_public_ip.vpn.id
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.vpn.id
  }
}
