#!/bin/bash

# Ejemplos de llamadas cURL para probar los microservicios de Usuario, Auth y Pedido
# Este archivo contiene ejemplos de cómo interactuar con las APIs

# ============================================================================
# SERVICIO DE USUARIO (Puerto 18001)
# ============================================================================

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║        EJEMPLOS DE USUARIO SERVICE                            ║"
echo "╚════════════════════════════════════════════════════════════════╝"

# 1. CREAR USUARIO
echo -e "\n1️⃣  Crear usuario"
echo "---"
curl -X POST http://localhost:18001/usuario-service/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "telefono": "1123456789",
    "direccion": "Calle Principal 123",
    "ciudad": "Buenos Aires",
    "pais": "Argentina"
  }' | jq .

# 2. CREAR OTRO USUARIO
echo -e "\n2️⃣  Crear otro usuario"
echo "---"
curl -X POST http://localhost:18001/usuario-service/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@example.com",
    "nombre": "María",
    "apellido": "García",
    "telefono": "1198765432",
    "direccion": "Avenida Secundaria 456",
    "ciudad": "Buenos Aires",
    "pais": "Argentina"
  }' | jq .

# 3. OBTENER TODOS LOS USUARIOS
echo -e "\n3️⃣  Obtener todos los usuarios"
echo "---"
curl -X GET http://localhost:18001/usuario-service/api/v1/usuarios | jq .

# 4. OBTENER USUARIO POR ID
echo -e "\n4️⃣  Obtener usuario por ID (ID=1)"
echo "---"
curl -X GET http://localhost:18001/usuario-service/api/v1/usuarios/1 | jq .

# 5. OBTENER USUARIO POR EMAIL
echo -e "\n5️⃣  Obtener usuario por email"
echo "---"
curl -X GET http://localhost:18001/usuario-service/api/v1/usuarios/email/juan@example.com | jq .

# 6. VERIFICAR SI USUARIO EXISTE
echo -e "\n6️⃣  Verificar si usuario existe (ID=1)"
echo "---"
curl -X GET http://localhost:18001/usuario-service/api/v1/usuarios/1/existe | jq .

# 7. OBTENER INFORMACIÓN BÁSICA DEL USUARIO
echo -e "\n7️⃣  Obtener información básica (ID=1)"
echo "---"
curl -X GET http://localhost:18001/usuario-service/api/v1/usuarios/1/info-basica | jq .

# 8. ACTUALIZAR USUARIO
echo -e "\n8️⃣  Actualizar usuario (ID=1)"
echo "---"
curl -X PUT http://localhost:18001/usuario-service/api/v1/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Carlos",
    "apellido": "Pérez López",
    "telefono": "1122334455",
    "direccion": "Calle Nueva 789",
    "ciudad": "La Plata",
    "pais": "Argentina"
  }' | jq .

# ============================================================================
# SERVICIO DE AUTH (Puerto 18003)
# ============================================================================

echo -e "\n╔════════════════════════════════════════════════════════════════╗"
echo "║        EJEMPLOS DE AUTH SERVICE                               ║"
echo "╚════════════════════════════════════════════════════════════════╝"

echo -e "\n9️⃣  Crear usuario de autenticación"
echo "---"
curl -X POST http://localhost:18003/auth-service/create-user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "123456"
  }' | jq .

echo -e "\n🔟 Login y obtención de token"
echo "---"
TOKEN=$(curl -s -X POST http://localhost:18003/auth-service/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "123456"
  }' | jq -r '.token')
echo "Token obtenido: $TOKEN"

echo -e "\n1️⃣1️⃣ Intento sin token (debe devolver 401)"
echo "---"
curl -i -X GET http://localhost:18002/pedido-service/api/v1/pedidos

echo -e "\n1️⃣2️⃣ Intento con token inválido (debe devolver 401)"
echo "---"
curl -i -X GET http://localhost:18002/pedido-service/api/v1/pedidos \
  -H "Authorization: Bearer token-invalido"

# ============================================================================
# SERVICIO DE PEDIDO (Puerto 18002)
# ============================================================================

echo -e "\n╔════════════════════════════════════════════════════════════════╗"
echo "║        EJEMPLOS DE PEDIDO SERVICE                              ║"
echo "╚════════════════════════════════════════════════════════════════╝"

# 13. CREAR PEDIDO (con validación de usuario)
echo -e "\n1️⃣3️⃣ Crear pedido para Usuario ID=1"
echo "---"
echo "⚠️  Nota: Esto valida que el usuario exista en el servicio de Usuario"
curl -X POST http://localhost:18002/pedido-service/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "usuarioId": 1,
    "numeroProducto": "PROD-001",
    "nombreProducto": "Laptop Dell XPS",
    "cantidad": 1,
    "precioUnitario": 1200.00,
    "descripcion": "Laptop de gama alta con procesador i7",
    "direccionEnvio": "Calle Principal 123, Buenos Aires"
  }' | jq .

# 14. CREAR OTRO PEDIDO
echo -e "\n1️⃣4️⃣ Crear otro pedido para Usuario ID=2"
echo "---"
curl -X POST http://localhost:18002/pedido-service/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "usuarioId": 2,
    "numeroProducto": "PROD-002",
    "nombreProducto": "Monitor LG 27 pulgadas",
    "cantidad": 2,
    "precioUnitario": 350.00,
    "descripcion": "Monitor 4K 60Hz",
    "direccionEnvio": "Avenida Secundaria 456, Buenos Aires"
  }' | jq .

# 15. OBTENER TODOS LOS PEDIDOS
echo -e "\n1️⃣5️⃣ Obtener todos los pedidos"
echo "---"
curl -X GET http://localhost:18002/pedido-service/api/v1/pedidos \
  -H "Authorization: Bearer $TOKEN" | jq .

# 16. OBTENER PEDIDO POR ID
echo -e "\n1️⃣6️⃣ Obtener pedido por ID (ID=1)"
echo "---"
curl -X GET http://localhost:18002/pedido-service/api/v1/pedidos/1 \
  -H "Authorization: Bearer $TOKEN" | jq .

# 17. OBTENER PEDIDO CON DETALLES DEL USUARIO (COMUNICACIÓN INTER-MICROSERVICIOS)
echo -e "\n1️⃣7️⃣ Obtener pedido con detalles del usuario (ID=1)"
echo "---"
echo "⚠️  Nota: Esta llamada realiza comunicación entre microservicios"
echo "Obtiene información del Usuario del servicio de Usuario automáticamente"
curl -X GET http://localhost:18002/pedido-service/api/v1/pedidos/1/detalles \
  -H "Authorization: Bearer $TOKEN" | jq .

# 18. OBTENER PEDIDOS POR USUARIO
echo -e "\n1️⃣8️⃣ Obtener pedidos del Usuario ID=1"
echo "---"
curl -X GET http://localhost:18002/pedido-service/api/v1/pedidos/usuario/1 \
  -H "Authorization: Bearer $TOKEN" | jq .

# 19. OBTENER PEDIDOS POR ESTADO
echo -e "\n1️⃣9️⃣ Obtener pedidos en estado PENDIENTE"
echo "---"
curl -X GET http://localhost:18002/pedido-service/api/v1/pedidos/estado/PENDIENTE \
  -H "Authorization: Bearer $TOKEN" | jq .

# 20. CONFIRMAR PEDIDO
echo -e "\n2️⃣0️⃣ Confirmar pedido (ID=1)"
echo "---"
curl -X PUT http://localhost:18002/pedido-service/api/v1/pedidos/1/confirmar \
  -H "Authorization: Bearer $TOKEN" | jq .

# 21. ACTUALIZAR ESTADO A EN_PROCESO
echo -e "\n2️⃣1️⃣ Actualizar pedido a EN_PROCESO (ID=1)"
echo "---"
curl -X PUT "http://localhost:18002/pedido-service/api/v1/pedidos/1/estado?nuevoEstado=EN_PROCESO" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 22. ACTUALIZAR ESTADO A ENVIADO
echo -e "\n2️⃣2️⃣ Actualizar pedido a ENVIADO (ID=1)"
echo "---"
curl -X PUT "http://localhost:18002/pedido-service/api/v1/pedidos/1/estado?nuevoEstado=ENVIADO" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 23. ACTUALIZAR ESTADO A ENTREGADO
echo -e "\n2️⃣3️⃣ Actualizar pedido a ENTREGADO (ID=1)"
echo "---"
curl -X PUT "http://localhost:18002/pedido-service/api/v1/pedidos/1/estado?nuevoEstado=ENTREGADO" \
  -H "Authorization: Bearer $TOKEN" | jq .

# 24. CANCELAR PEDIDO
echo -e "\n2️⃣4️⃣ Cancelar pedido (ID=2)"
echo "---"
curl -X PUT http://localhost:18002/pedido-service/api/v1/pedidos/2/cancelar \
  -H "Authorization: Bearer $TOKEN" | jq .

# ============================================================================
# SWAGGER DOCUMENTATION
# ============================================================================

echo -e "\n╔════════════════════════════════════════════════════════════════╗"
echo "║        DOCUMENTACIÓN SWAGGER                                   ║"
echo "╚════════════════════════════════════════════════════════════════╝"

echo -e "\n📚 Accede a las documentaciones:"
echo "   Usuario Service: http://localhost:18001/usuario-service/swagger-ui.html"
echo "   Pedido Service:  http://localhost:18002/pedido-service/swagger-ui.html"
echo "   Auth Service:    http://localhost:18003/auth-service/swagger-ui.html"

echo -e "\n✅ ¡Ejemplos completados!"
