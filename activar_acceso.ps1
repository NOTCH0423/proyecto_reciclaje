Write-Host "=== Configurando Acceso Remoto para el Chatbot ===" -ForegroundColor Cyan

# 1. Cambiar la red Wi-Fi a 'Privada' (necesario para que Windows permita conexiones entrantes)
try {
    Write-Host "1. Cambiando perfil de red Wi-Fi a Privado..."
    Set-NetConnectionProfile -InterfaceAlias "Wi-Fi" -NetworkCategory Private -ErrorAction Stop
    Write-Host "   [OK] Red configurada como Privada." -ForegroundColor Green
} catch {
    Write-Host "   [!] No se pudo cambiar el perfil de red (¿Quizás ya es privado o necesitas permisos de Admin?)." -ForegroundColor Yellow
}

# 2. Abrir el puerto 8081 en el Firewall
try {
    Write-Host "2. Abriendo puerto 8081 en el Firewall..."
    New-NetFirewallRule -DisplayName "Reciclaje Chatbot Web" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow -Profile Any -ErrorAction Stop
    Write-Host "   [OK] Puerto 8081 abierto correctamente." -ForegroundColor Green
} catch {
    Write-Host "   [ERROR] No se pudo crear la regla de firewall. Asegúrate de ejecutar este script como ADMINISTRADOR." -ForegroundColor Red
}

Write-Host "`n=== Configuración Finalizada ===" -ForegroundColor Cyan
Write-Host "Por favor, intenta acceder desde tu celular a: http://192.168.0.15:8081"
Write-Host "Presiona Enter para salir..."
Read-Host