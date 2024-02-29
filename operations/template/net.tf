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

#resource "azurerm_private_dns_zone_virtual_network_link" "db_network_link" {
#  name                  = "intermediarylink.com"
#  private_dns_zone_name = azurerm_private_dns_zone.dns_zone.name
#  virtual_network_id    = data.azurerm_virtual_network.app.id
#  resource_group_name   = data.azurerm_resource_group.group.name
#  depends_on            = [azurerm_subnet.database]
#}

resource "azurerm_network_security_group" "db_security_group" {
  name                = "database-security-group"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name
}


resource "azurerm_network_security_rule" "DB_Splunk_UF_omhsinf" {
  name                        = "DB_Splunk_UF_omhsinf"
  priority                    = 103
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "9997-9998"
  source_address_prefixes     = ["10.65.8.211/32","10.65.8.212/32","10.65.7.212/32","10.65.7.211/32","10.65.8.210/32","10.65.7.210/32"]
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}

resource "azurerm_network_security_rule" "DB_Splunk_Indexer_Discovery_omhsinf" {
  name                        = "DB_Splunk_Indexer_Discovery_omhsinf"
  priority                    = 104
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "8089"
  source_address_prefix       = "10.11.7.22/32"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}


resource "azurerm_network_security_rule" "DB_Safe_Encase_Monitoring_omhsinf" {
  name                        = "DB_Safe_Encase_Monitoring_omhsinf"
  priority                    = 105
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "34445"
  source_address_prefix       = "10.11.6.145/32"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}

resource "azurerm_network_security_rule" "DB_ForeScout_Manager_omhsinf" {
  name                        = "DB_ForeScout_Manager_omhsinf"
  priority                    = 106
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_ranges     = ["556","443","10003-10006"]
  source_address_prefixes     = ["10.64.8.184","10.64.8.180/32"]
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}

resource "azurerm_network_security_rule" "DB_BigFix_omhsinf" {
  name                        = "DB_BigFix_omhsinf"
  priority                    = 107
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "52314"
  source_address_prefix       = "10.11.4.84/32"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}


resource "azurerm_network_security_rule" "DB_Allow_All_Out_omhsinf" {
  name                        = "DB_Allow_All_Out_omhsinf"
  priority                    = 109
  direction                   = "Outbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_ranges     = ["5432"]
  source_address_prefix       = "VirtualNetwork"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}

resource "azurerm_network_security_rule" "db_outbound_allow" {
  name                        = "db_outbound_allow"
  priority                    = 110
  direction                   = "Outbound"
  access                      = "Allow"
  protocol                    = "*"
  source_port_range           = "*"
  destination_port_range      = "*"
  source_address_prefix       = "*"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}

resource "azurerm_network_security_rule" "db_inbound_allow" {
  name                        = "db_inbound_allow"
  priority                    = 111
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_ranges     = ["5432"]
  source_address_prefix       = "VirtualNetwork"
  destination_address_prefix  = "VirtualNetwork"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.db_security_group.name
}

resource "azurerm_subnet_network_security_group_association" "database_security_group" {
  subnet_id                 = azurerm_subnet.database.id
  network_security_group_id = azurerm_network_security_group.db_security_group.id
}

resource "azurerm_network_security_group" "app_security_group" {
  name                = "app-security-group"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name
}


resource "azurerm_network_security_rule" "App_Splunk_UF_omhsinf" {
  name                        = "App_Splunk_UF_omhsinf"
  priority                    = 103
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "9997-9998"
  source_address_prefixes     = ["10.65.8.211/32","10.65.8.212/32","10.65.7.212/32","10.65.7.211/32","10.65.8.210/32","10.65.7.210/32"]
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}

resource "azurerm_network_security_rule" "App_Splunk_Indexer_Discovery_omhsinf" {
  name                        = "App_Splunk_Indexer_Discovery_omhsinf"
  priority                    = 104
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "8089"
  source_address_prefix       = "10.11.7.22/32"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}


resource "azurerm_network_security_rule" "App_Safe_Encase_Monitoring_omhsinf" {
  name                        = "App_Safe_Encase_Monitoring_omhsinf"
  priority                    = 105
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "34445"
  source_address_prefix       = "10.11.6.145/32"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}

resource "azurerm_network_security_rule" "App_ForeScout_Manager_omhsinf" {
  name                        = "App_ForeScout_Manager_omhsinf"
  priority                    = 106
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_ranges     = ["556","443","10003-10006"]
  source_address_prefixes     = ["10.64.8.184","10.64.8.180/32"]
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}

resource "azurerm_network_security_rule" "App_BigFix_omhsinf" {
  name                        = "App_BigFix_omhsinf"
  priority                    = 107
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "52314"
  source_address_prefix       = "10.11.4.84/32"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}


resource "azurerm_network_security_rule" "App_Allow_All_Out_omhsinf" {
  name                        = "App_Allow_All_Out_omhsinf"
  priority                    = 109
  direction                   = "Outbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_ranges     = ["80","443"]
  source_address_prefix       = "VirtualNetwork"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}

resource "azurerm_network_security_rule" "app_outbound_allow" {
  name                        = "app_outbound_allow"
  priority                    = 110
  direction                   = "Outbound"
  access                      = "Allow"
  protocol                    = "*"
  source_port_range           = "*"
  destination_port_range      = "*"
  source_address_prefix       = "*"
  destination_address_prefix  = "*"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}

resource "azurerm_network_security_rule" "app_inbound_allow" {
  name                        = "app_inbound_allow"
  priority                    = 111
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_ranges     = ["80","443"]
  source_address_prefix       = "VirtualNetwork"
  destination_address_prefix  = "VirtualNetwork"
  resource_group_name         = data.azurerm_resource_group.group.name
  network_security_group_name = azurerm_network_security_group.app_security_group.name
}

resource "azurerm_subnet_network_security_group_association" "app_security_group" {
  subnet_id                 = azurerm_subnet.app.id
  network_security_group_id = azurerm_network_security_group.app_security_group.id
}
