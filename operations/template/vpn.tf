
resource "azurerm_public_ip" "vpn" {
  count               = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? 1 : 0
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
    public_ip_address_id          = azurerm_public_ip.vpn.id
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.vpn.id
  }

  vpn_client_configuration {
    address_space = ["192.168.0.0/16"]
    vpn_auth_types = ["Certificate"]
    vpn_client_protocols = ["OpenVPN"]

    root_certificate {
      name = "vpn-cert"
      public_cert_data = "MIIC5jCCAc6gAwIBAgIIGMy2CjfbdWEwDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGVlBOIENBMB4XDTI0MDMwNTIxMTIzOVoXDTI3MDMwNTIxMTIzOVowETEPMA0GA1UEAxMGVlBOIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnF+rF0RxT8mFwVwQY976uEyR1dr6bC7G+18fJuk8yEG+vVhVrCldSCMNL4QkeZNvBNW4/W2DsuGVKFwc8u3iII/uSQ7ANU1EsFve0GdlSQv8gHYAWwKaR2Rt20uaFMBkeWIUScrMtesd+AvBk5h2Ll6opNR4SGOZSkH6lGl8KMWbUuQbiME4RIF07bNMF+fNHRXxsUMM6OWRzDS8VZaEAz8iuKr9qGi4hIB2dQlJa8fGgU+J5gt2C33t56VaJsde2/MJtTj9P/8elTeGpfPATMNqCThYK3UsfRe5Jrl/wHlugVhPLqwOKwrvVd+Vv3vTkiPhfIhPu37aoHYGZyMpfQIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQUOUXvWrL8eFMpB8Mc0GJKcySw56UwDQYJKoZIhvcNAQELBQADggEBAEkXMQ4iQ9pK3DutiwY1ejEShF+O0agMGaLDcKNFtNlc4UwN10RNkkBEPLSZhuBKGkBQIxuNLqsFUaZcL4x47a2VQUBuJhXvIXfNtNupYcRusVyBRSYbcZWpernbXSRutCUfO24tRsMG9m+QBAmJYU6XNDQUi55CwAhygg7mnARdcRZAP7qBVUr/ga59mWVVWWcO5VfKQD2XfBp88AOwkw/C9odX1bIfIEu+A9KWrvVh2eqMDmTb4sEOQcuP70kBU3udckPb51a4R4J/LxuDfWjAXrJGHA/W6srbO8FIkRvsUKHk5CEH487+gMKI9Jt70mG2dwdHTqiqE1VY6z3VDbs="  # pragma: allowlist secret
    }
  }
}

resource "azurerm_private_dns_resolver" "private_zone_resolver" {
  count               = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? 1 : 0
  name                = "private-resolve-${var.environment}"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  virtual_network_id  = data.azurerm_virtual_network.app.id
}


resource "azurerm_private_dns_resolver_inbound_endpoint" "resolver_inbound_endpoint" {
  count                   = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? 1 : 0
  name                    = "endpoint-inbound-${var.environment}"
  private_dns_resolver_id = azurerm_private_dns_resolver.private_zone_resolver.id
  location                = azurerm_private_dns_resolver.private_zone_resolver.location

  ip_configurations {
    private_ip_allocation_method = "Dynamic"
    subnet_id                    = azurerm_subnet.resolver_inbound.id
  }
}

resource "azurerm_private_dns_resolver_outbound_endpoint" "resolver_outbound_endpoint" {
  count                   = var.environment == "dev" || var.environment == "stg" || var.environment == "prd" ? 1 : 0
  name                    = "endpoint-outbound-${var.environment}"
  private_dns_resolver_id = azurerm_private_dns_resolver.private_zone_resolver.id
  location                = azurerm_private_dns_resolver.private_zone_resolver.location
  subnet_id               = azurerm_subnet.resolver_outbound.id
}
