terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.7.0"
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
      purge_soft_deleted_keys_on_destroy    = false
    }
  }
}

module "template" {
  source = "../../template/"

  environment          = "prd"
  deployer_id          = "f5feabe7-5d37-40ba-94f2-e5c0760b4561"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 //github app registration in CDC Azure Entra
  vpn_root_certificate = "MIIC5jCCAc6gAwIBAgIIcxi1RO+5LvAwDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGVlBOIENBMB4XDTI0MDkwNTIwMzczMVoXDTI3MDkwNTIwMzczMVowETEPMA0GA1UEAxMGVlBOIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy3yPSImArdeGbn98TyyavSDAbE8Bh1TyppXFxt8wHrm+iM7zVTAHVt7Zvfs/hpPYpMazSUaiURvvtGdyFlNeFztdhmrrdNhjH44K9mVcEso7d/CuioImjW8gAlp7k8QuEqmjS5KNJPd83g7OTxDWKQJvamhqdySxf3+B3CvQsj2CIrEgbk8ALwFkIM46jwLgIZwN4hTdyvnbLL4EKxg0TTic8vi+OrGL5h6uxCSsItbQw9VkidMfZfRsY0NJybEykezt8D8qcyYQQlllZVjT8wDMyZte94MO0QCeny1KgUsHkl/LnXe9hA+dYmoEl5DoaM5HbIQV/lm/Nz3EzFPfdQIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQUcZv3ASlNMBCLnysNKodtMs4uTNswDQYJKoZIhvcNAQELBQADggEBALFXSNh2SDr9cMG3UeiCZ3dJLxFImagmkS1nEpPiSMbhI+kEQJOIPECWGZ1WDAHWeRLp07blnfpy+xaiKzsR2vRUyqVHNg0xp2PpW/RzjqeIYF9nRIAEhJZ463bXJChnhSwCIs62ov/MRo4VR4resOgrJay7grNHaJgr/+MgNxHFH32p3DmJRN+u88bOm00LjixcUJn7IrQpuJAy0IdoXX0r9tV5kkCbtQuaFtopvx4G08IvHLxiXXkoYZDFTujH47JXzEFj3gmMUfrSO3WABaxIYOVz3YUoqWDGaw1yG/b9QNXeJr2V9TFzmTTRzS9yZIfylMcENtq5kC9Bl+3BgkI=" # pragma: allowlist secret
  alert_slack_email    = var.alert_slack_email
}
