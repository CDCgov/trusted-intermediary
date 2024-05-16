variable "pr_number" {
  type     = string
  nullable = false
}

variable "alert_slack_email" {
  type = string
  nullable = false
  sensitive = true
}
