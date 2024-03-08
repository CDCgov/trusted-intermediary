
resource "azurerm_public_ip" "vpn" {
  name                = "vpn-public-ip"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  allocation_method = "Dynamic"
}

resource "azurerm_virtual_network_gateway" "vpn" {
  count               = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? 1 : 0
  name                = "${var.environment}-vpn"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  type     = "Vpn"
  vpn_type = "RouteBased"

  active_active = false
  enable_bgp    = false
  sku           = "VpnGw1"

  ip_configuration {
    public_ip_address_id          = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? azurerm_public_ip.vpn.id : null
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.vpn.id
  }

  vpn_client_configuration {
    address_space = ["192.168.0.0/16"]
    vpn_auth_types = ["Certificate"]
    vpn_client_protocols = ["OpenVPN"]

    root_certificate {
      name = "vpn-cert"
      public_cert_data = var.vpn_root_certificate
    }
  }
}

resource "azurerm_private_dns_resolver" "private_zone_resolver" {
  name                = "private-resolve-${var.environment}"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  virtual_network_id  = data.azurerm_virtual_network.app.id
}


resource "azurerm_private_dns_resolver_inbound_endpoint" "resolver_inbound_endpoint" {
  name                    = "endpoint-inbound-${var.environment}"
  private_dns_resolver_id = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? azurerm_private_dns_resolver.private_zone_resolver.id : null
  location                = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? azurerm_private_dns_resolver.private_zone_resolver.location : null

  ip_configurations {
    private_ip_allocation_method = "Dynamic"
    subnet_id                    = azurerm_subnet.resolver_inbound.id
  }
}

resource "azurerm_private_dns_resolver_outbound_endpoint" "resolver_outbound_endpoint" {
  name                    = "endpoint-outbound-${var.environment}"
  private_dns_resolver_id = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? azurerm_private_dns_resolver.private_zone_resolver.id : null
  location                = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? azurerm_private_dns_resolver.private_zone_resolver.location: null
  subnet_id               = azurerm_subnet.resolver_outbound.id
}
