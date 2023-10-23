output "registry" {
  value = azurerm_container_registry.registry.login_server
}

output "publish_app" {
  value = azurerm_linux_web_app.api.name
}
