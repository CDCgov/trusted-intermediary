terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.103.1"
    }
  }

  # Use a remote Terraform state in Azure Storage
  backend "azurerm" {
    resource_group_name  = "csels-rsti-prd-moderate-rg"
    storage_account_name = "cdcintermediarytrfrmprd"
    container_name       = "terraform-state"
    key                  = "prd.terraform.tfstate"
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

  environment          = "prd"
  deployer_id          = "f5feabe7-5d37-40ba-94f2-e5c0760b4561"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 //github app registration in CDC Azure Entra
  vpn_root_certificate = "MIIC5jCCAc6gAwIBAgIIeHnOQDhz00AwDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGVlBOIENBMB4XDTI0MDMwODE1NDA1M1oXDTI3MDMwODE1NDA1M1owETEPMA0GA1UEAxMGVlBOIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkgeXZ6ReEQ5HAqlXULUUdVfCMtMPmlTeCFFkhD9i5E5lRg78PyJqczHMzCB6l83O/PrLWXjT3/s/R58cfeHJg/SndGwt/2uKhj1kNW7Ivc8kF0pgSL3lDR+NSj5OPda45EY30ZlTjgygmb9MjfCT2BmgjGcfUbgm0jzgDZsk7bLUUJkL38DJP+v2M6sDxyxMjoY9gJ1Kq5Fg81serJlZHaACShuuhgiKqH3+hwvIPluK8Y40FWfiKpGRjdkAXGTmB+afMeA4L1amyticIPzzOytIHFIDMOKgJRL62UQe+alzubXkYbDtEgDCOwF8k5TRiu9MUwID34CLkp2VWnLnUwIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQUHyrypPmh+KVb2sspeGsxboG1hQwwDQYJKoZIhvcNAQELBQADggEBAGmfFRLgqLQxedGHeXQoajHzhCvk+62lDR1xy0s2mklA3eRxzOyaXRPgmM6lbGBm6LdLxo5nxGgfD4h2vOBZl4MXOFLryLm97QtDZ34YkxGn+tugUAXpWBB/EJIynib1Ywyg6Kv6g3oYjf2bc8Ae9bOWGR0FtOGn8TvmSzKLXoUwQd0u9DEA774YtpvPxHxw69uyf8x2nekpyWNyFbR6DWJEA9M+BHeR0oGEGoc5FH6zTgstbdeNVou3NNQlRKlWD26vWeCeQvbKDK5+KuOPjjDTimGdx1GfA9z/ai/pX+K/NKvvC4JXQdW7jYYu3QFglP70esT9mBCxVQbXd49oD9M=" # pragma: allowlist secret
}
