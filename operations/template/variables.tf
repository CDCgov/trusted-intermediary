variable "environment" {
  type     = string
  nullable = false
}

variable "deployer_id" {
  type     = string
  nullable = false
}

variable "vpn_root_certificate" {
  type     = string
  nullable = true
  default = null
}
