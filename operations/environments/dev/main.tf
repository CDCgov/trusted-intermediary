terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.100.0"
    }
  }

  # Use a remote Terraform state in Azure Storage
  backend "azurerm" {
    resource_group_name  = "csels-rsti-dev-moderate-rg"
    storage_account_name = "cdcintermediaryterraform"
    container_name       = "terraform-state"
    key                  = "dev.terraform.tfstate"
  }
}

# Configure the Microsoft Azure Provider
provider "azurerm" {
  features {
    key_vault {
      purge_soft_deleted_secrets_on_destroy = false
    }
  }
}

module "template" {
  source = "../../template/"

  environment          = "dev"
  deployer_id          = "f5feabe7-5d37-40ba-94f2-e5c0760b4561"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 //github app registration in CDC Azure Entra
  vpn_root_certificate = "MIIC5jCCAc6gAwIBAgIIWvb3sLkOQtcwDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGVlBOIENBMB4XDTI0MDMwODE1MjM0OFoXDTI3MDMwODE1MjM0OFowETEPMA0GA1UEAxMGVlBOIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt1v2bAATE7IOJkqUrbwQw6X99fi3Ywf1bv0uZ0gGDjG10H+PB2BUzZ94RNcB4Oezi6t+/WAQUkhRozemFkegSkfKHEehAT6nu6OBXKt2rH/oJtpKR791ab9H9aQ6e5LO9OZ237QL6XikhGG7HXqG9ndYnhBYPy2/pd8VV6ZwqMR3PkfBJaC4tKy4d8dim+PMpT5rqPGbsf9H+dydvG6JOKZiHb3/yqi6fqoise1yY64aDwFC9MbEbtgXpvmBFsei2PA/XH5FqE6F/kyCg7mO5TSYYEqx0PCTPmICAT4iw5ELMyAhVKL2OpMjqw5YAYr/TGqlfyEYpBBQMvC3K9OmUwIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQU5idI+AFsHn8BjvNBE5ShE+aFor0wDQYJKoZIhvcNAQELBQADggEBAJkDtuHj4QGyXtooiM7xfHZ/lGdDZvF+KAVfFKAlsIO8y1NS2iAeNT6MmampwzWzXIUMk9vxvALUoh2MJkWP5CX2e3vDj2lGpbhK5//rfWDin1/jj28+KZzSVsk4i/EkdBWW7eCKU401rafVOjSLmM5mfDTAHNrFxzQWJF5WL7TxrQw7chrnpy4v0V7/y4h+QsQja8LXx9keEdB2BQSjndAqxB9dblFALantpuEOM2pS3GCaC2REXSnKsgSEQoVL07MSndpCpdv5bsEkppM5LBC6gL66a43Lho3kSCm4ZU51mjJtNwadeBXpHjkJ1yiBA7CG/Roa+THAiV+VMP75g3E=" # pragma: allowlist secret
  alert_slack_webhook = var.alert_slack_webhook
}
