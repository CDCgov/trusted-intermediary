terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.4.0"
    }
  }

  # Use a remote Terraform state in Azure Storage
  backend "azurerm" {
    resource_group_name  = "csels-rsti-stg-moderate-rg"
    storage_account_name = "cdcintermediarytrfrmstg"
    container_name       = "terraform-state"
    key                  = "staging.terraform.tfstate"
  }
}

# Configure the Microsoft Azure Provider
provider "azurerm" {
  features {
    key_vault {
      purge_soft_deleted_secrets_on_destroy = false
      purge_soft_deleted_keys_on_destroy    = false
    }
  }
}

module "template" {
  source = "../../template/"

  environment          = "stg"
  deployer_id          = "f5feabe7-5d37-40ba-94f2-e5c0760b4561"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 //github app registration in CDC Azure Entra
  vpn_root_certificate = "MIIC5jCCAc6gAwIBAgIISASFFP2pD+4wDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGVlBOIENBMB4XDTI0MDkwNTIwMzcxOVoXDTI3MDkwNTIwMzcxOVowETEPMA0GA1UEAxMGVlBOIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyowIBh4cKu2Bfl16lPl9Yjx/6UrhttTOgYLkk6kAxI4vIXWtDcYMWB/5QHTAhgaXkBfyfwRD7WA0v+kjORI+O/AUMJeWszbF/WW/eVlWy10bsjuSkS6/xdPgvr8qGB4me9bJ1IVwfMFQTgZiDOxTwHs/Kd9l3IkKSlcLKhuCopZYmzKGGdisHGzY53UZ4pDMTAU112Iy/njbCGdgO7Rnx9+ghoFNNE2ljPG1k8j/ciufTnBbbmaj7TwVMULNjL3FQ5Iv/Xxtetk02XNjAXpjW3o05hqIOBZQdN8CzdAEh+JViOLDol+LoWPvPm8Uw+RTDZwZyMeAfuY8XCz7aou++QIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQUr/c0Rl869FPnXw34AacFv8d3/WYwDQYJKoZIhvcNAQELBQADggEBAHDqLD8UkumYBxaARagAmRWefSf9HltZR8K/3WjAw2e1hwh+OBQ8wbHwu/OpTJ4Tw74J4o2gLd3xWx0P/J/NzCZHR8Q/+d3D5Kc8uOWOVnaHUh11AL3nCW/YFUHX6QGJ6JC+h/RJrtLQHzR1P6fGtbUT8R0bMUWfhYtHmgL6znflN525KcW76eEhdH74q4SG8k7KX7JnMcsNSvk/ht0wQZ4vjKnycZBxtGLjT8VzSGgKEq7h0wP+CI3oD8WEOn4xP/t/lStoykyBEsJyG37CxkZxJPChea2nwArrkzK1FvQR4VehhZ+QCG+jho/dF8kcC53TeFgOrZVPT/Z5+vN7dJU=" # pragma: allowlist secret
  alert_slack_email    = var.alert_slack_email
}
