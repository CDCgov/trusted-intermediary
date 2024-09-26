terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.3.0"
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
      purge_soft_deleted_keys_on_destroy    = false
    }
  }
}

module "template" {
  source = "../../template/"

  environment          = "dev"
  deployer_id          = "f5feabe7-5d37-40ba-94f2-e5c0760b4561"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 //github app registration in CDC Azure Entra
  vpn_root_certificate = "MIIC5jCCAc6gAwIBAgIIUWYBCfn9YmgwDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGVlBOIENBMB4XDTI0MDkwNTE4MzczOFoXDTI3MDkwNTE4MzczOFowETEPMA0GA1UEAxMGVlBOIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuiovPDT47dNOgzusC24VbavxEA6UssRWjDKGgubY/5nBpcpQ4x2j5BEJeUBR2KxH7idPnQmAWSQitRr+usynoU3Dd47GiY+QKW8tFZqcVwnyOShnbKPdYT4F5VN05Veda/K7LOgpstEC9tBNoJ5ECxHZxCAUDYK+Cp3y/OgS1GPpBlENnnHM61MZfkh8VFyjWKlqYM1wl6A5YlMtdragqv3tUAI13T21vEe9YK0LIi9nkd7sxbYvrweZlVxkMpR40oeYHSpSoy+dta74nr48q4/Doa4MsIMeKww0au8uWHVw1Nvf8DzRdz14xVgWkb9XHZALnrpdjxRDjOh5IPNj3QIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQUFQgdnFvmPYcqckkFOJhbHsqjN5EwDQYJKoZIhvcNAQELBQADggEBAGbxut0fhAQ4b/T6q9BR+fIdZIynHAgndk3TRe4eUuxFtQKfWPwIRw1L5b1PWVxtb//kCTAH1nTsOmlv0hEfLS7EqDxDidwp5vF+Y4wSlAJMecCp2OCh654D1/Hnlo1M9Z5M0ZLX0ynQ+aCuK0qrBB0te+aGntiBPuUdRMevBVMuVrqXVeX7MBpThqNYsqtgKUoxI3GtV2s5wNkh9KqEVX+KDREjQ8xY9gdFUjpChdoeLB8p+Jr37z5E/mQEpJAxQ3AcEsyUqadNRztLJYWKHevZJSwThsW9vc8SnLniugrixbNgkoJtyAoHw+6p/xAOcj8B2oyk4eMIXm4+937pJMM=" # pragma: allowlist secret
  alert_slack_email    = var.alert_slack_email
}
