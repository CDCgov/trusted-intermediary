output "registry" {
  value = module.template.registry
}

output "acr_username" {
  value     = module.template.acr_username
  sensitive = true
}

output "acr_password" {
  value     = module.template.acr_password
  sensitive = true
}

output "publish_app" {
  value = module.template.publish_app
}
