variable "pr_number" {
  type     = string
  nullable = false
}

variable "alert_slack_webhook" {
  type = string
  nullable = false
  sensitive = true
}
