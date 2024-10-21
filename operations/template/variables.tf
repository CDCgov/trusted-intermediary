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
  default  = null
}

variable "alert_slack_email" {
  type      = string
  nullable  = false
  sensitive = true
}

variable "service_health_locations" {
  type    = list(string)
  default = ["global"]
}
