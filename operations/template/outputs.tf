# Output Container Registry information

output "registry" {
  value = azurerm_container_registry.registry.login_server
}

output "acr_username" {
  value     = azurerm_container_registry.registry.admin_username
  sensitive = true
}

output "acr_password" {
  value     = azurerm_container_registry.registry.admin_password
  sensitive = true
}


# Output App Service information

output "publish_app" {
  value = azurerm_linux_web_app.api.name
}
