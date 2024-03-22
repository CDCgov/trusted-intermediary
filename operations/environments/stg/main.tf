terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.97.0"
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
    }
  }
}

module "template" {
  source = "../../template/"

  environment          = "stg"
  deployer_id          = "f5feabe7-5d37-40ba-94f2-e5c0760b4561"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 //github app registration in CDC Azure Entra
  vpn_root_certificate = "MIIC5jCCAc6gAwIBAgIIUC720RvICDQwDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGVlBOIENBMB4XDTI0MDMwODE1MzY1MloXDTI3MDMwODE1MzY1MlowETEPMA0GA1UEAxMGVlBOIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtn+H6PbB2y/JmoTTNuxljVY7I02BblPmnzwzQhEPAZjoMVQTsEvS5rr/ILLFFF5FdTcyPYJpD5Tvd+w1v62xV4QhZSFpSSyfRvsi6uzOLOyDOhVN++GWAjKyTvaOO654JwX/qj7nHSYQLQFtnf9OkixZazO8o0snXpGCSYKgxhBox6+XyZpjjwoFt+wMrNalrAOWCtAp/pgIB7xyStcWyGEi7vACiV+7rzI2Kxh+PfaltS4wU1vWN7jN2GxMbVG3539ybiT4fpoGuDWjZ7t7tp1LgQa1n7tlvNR0W01pdt7U/fPL9ynfyuP8Wph8eetW9THYtJkBTNk7KyhE+z36TwIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQU85kmRn9Cnq9LjFeKjwrMUhgo3xowDQYJKoZIhvcNAQELBQADggEBAHjzfciR/TqJojJ3xd2AmMQev5Aw6Wf9gFfhv0eb9bmqyeJ23bYhOvqWxIxb01TBp5CNhWgWuUE68cQpEqafu9JOITDk9GtQ9m6/4sHOhzqM11beGqKlomQuT+I/M/gS0pUcr//W7riTkOQQI6DHKgpoGoRXpk9/V5GrwQauZjy1hRyRpVlg4xDgJJqRr5PKUErtA07DYck+AblJW4msglfyM2HTvvMLNdsmiZmjFdU1osT0WT/W9nY+RGadAo47x6qknpFoDoVtIQ3XNH3C5Scl1bGphfQdmEjNVhg7a8gSWat7n1OjFiz3OvTqy5MsssmRz4WlOM5+xOhiT2OambA=" # pragma: allowlist secret
}
