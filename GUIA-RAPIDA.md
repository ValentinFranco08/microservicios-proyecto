# 🚀 Guía Rápida de Inicio

## ⚡ 5 minutos para empezar

### 1. Prerequisitos
```bash
# Verificar que Docker está instalado
docker --version
docker-compose --version
```

### 2. Clonar y navegar
```bash
cd microservicios-proyecto
```

### 3. Iniciar los servicios
```bash
docker-compose up -d
```

### 4. Esperar a que estén listos (30 segundos)
```bash
docker-compose ps

# Deberías ver STATUS: Up y healthy
```

### 5. Acceder a las APIs
- **Swagger Usuario**: http://localhost:8001/usuario-service/swagger-ui.html
- **Swagger Pedido**: http://localhost:8002/pedido-service/swagger-ui.html

---

## 📝 Guía de Inicio Rápido - Primer Pedido

### Paso 1: Crear Usuario
```bash
curl -X POST http://localhost:8001/usuario-service/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "telefono": "1234567890",
    "direccion": "Calle 123",
    "ciudad": "Buenos Aires",
    "pais": "Argentina"
  }' | jq .
```

Respuesta: `"id": 1`

### Paso 2: Crear Pedido (valida automáticamente usuario)
```bash
curl -X POST http://localhost:8002/pedido-service/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 1,
    "numeroProducto": "PROD-001",
    "nombreProducto": "Laptop",
    "cantidad": 1,
    "precioUnitario": 1000.00,
    "descripcion": "Laptop de calidad",
    "direccionEnvio": "Calle 123, Buenos Aires"
  }' | jq .
```

Respuesta: `"id": 1, "estado": "PENDIENTE"`

### Paso 3: Ver pedido con datos del usuario
```bash
curl http://localhost:8002/pedido-service/api/v1/pedidos/1/detalles | jq .
```

✅ **¡Listo! Has creado tu primer pedido con comunicación inter-microservicios**

---

## ❓ Preguntas Frecuentes

### P: ¿Cómo veo los logs?
**R:**
```bash
# Todos los logs
docker-compose logs -f

# Solo Usuario Service
docker-compose logs -f usuario-service

# Solo Pedido Service
docker-compose logs -f pedido-service
```

### P: ¿Cómo accedo a las bases de datos?
**R:**
```bash
# Conectarse a PostgreSQL de Usuario
docker exec -it usuario-db psql -U usuario -d usuarios_db

# Ver usuarios
SELECT * FROM usuarios;

# Conectarse a PostgreSQL de Pedido
docker exec -it pedido-db psql -U pedido -d pedidos_db

# Ver pedidos
SELECT * FROM pedidos;
```

### P: ¿Cómo obtengo todos mis pedidos de un usuario?
**R:**
```bash
# Usuario ID = 1
curl http://localhost:8002/pedido-service/api/v1/pedidos/usuario/1 | jq .
```

### P: ¿Qué ocurre si intento crear un pedido con usuario inexistente?
**R:** El servicio validará con el Usuario Service y retornará un error:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "No se pudo conectar con el servicio de Usuario"
}
```

### P: ¿Cómo cambio el estado de un pedido?
**R:**
```bash
# Estado a CONFIRMADO
curl -X PUT "http://localhost:8002/pedido-service/api/v1/pedidos/1/estado?nuevoEstado=CONFIRMADO"

# Estados disponibles:
# PENDIENTE, CONFIRMADO, EN_PROCESO, ENVIADO, ENTREGADO, CANCELADO
```

### P: ¿Cómo agrego un nuevo usuario?
**R:**
```bash
curl -X POST http://localhost:8001/usuario-service/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@example.com",
    "nombre": "Nombre",
    "apellido": "Apellido",
    "telefono": "1234567890",
    "direccion": "Dirección",
    "ciudad": "Ciudad",
    "pais": "País"
  }'
```

### P: ¿Cómo detener los servicios?
**R:**
```bash
# Detener pero mantener volúmenes
docker-compose down

# Detener y eliminar volúmenes (BORRA DATOS)
docker-compose down -v
```

### P: ¿Cómo reiniciar un servicio específico?
**R:**
```bash
docker-compose restart usuario-service
docker-compose restart pedido-service
```

### P: ¿Puedo tener múltiples instancias de un servicio?
**R:** Sí, pero necesitas un Load Balancer. Para desarrollo:
```bash
# Nota: Esto no funcionará sin configuración adicional
docker-compose up -d --scale usuario-service=3
```

### P: ¿Cómo cambio el puerto de un servicio?
**R:** En docker-compose.yml, modifica la línea de ports:
```yaml
usuario-service:
  ports:
    - "9001:8001"  # Cambiar de 8001 a 9001
```

### P: ¿Cómo accedo a Swagger desde otra máquina?
**R:** Por defecto no está expuesto. Para acceso remoto, modifica en docker-compose:
```yaml
ports:
  - "0.0.0.0:8001:8001"  # En lugar de localhost
```

### P: ¿Cómo veo si hay errores de conexión?
**R:**
```bash
# Ver logs y buscar errores
docker-compose logs usuario-service | grep -i error

# Probar conectividad desde pedido a usuario
docker exec pedido-service curl http://usuario-service:8001/usuario-service/api/v1/usuarios
```

### P: ¿Cómo actualizo la información de un usuario?
**R:**
```bash
curl -X PUT http://localhost:8001/usuario-service/api/v1/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Nuevo Nombre",
    "apellido": "Nuevo Apellido",
    "telefono": "9876543210",
    "direccion": "Nueva Dirección",
    "ciudad": "Nueva Ciudad",
    "pais": "Nuevo País"
  }'
```

### P: ¿Cómo elimino un usuario?
**R:**
```bash
curl -X DELETE http://localhost:8001/usuario-service/api/v1/usuarios/1
```
**Nota:** Se realiza borrado lógico (no se elimina de la BD, solo se marca como inactivo)

### P: ¿Qué significa "comunicación inter-microservicios"?
**R:** Significa que un microservicio llama a otro a través de REST API. En este caso:
- Cuando creas un pedido, el Pedido Service consulta al Usuario Service
- Lo hace automáticamente sin que tengas que hacerlo manualmente

### P: ¿Cómo sé que la comunicación inter-microservicios está funcionando?
**R:** Mira los logs del Pedido Service:
```bash
docker logs pedido-service | grep "Verificando usuario"
```

Deberías ver líneas como:
```
Verificando usuario con ID: 1
Llamando a servicio de Usuario: http://usuario-service:8001/usuario-service/api/v1/usuarios/1
Usuario validado: Juan Pérez
```

### P: ¿Cómo configuro variables de entorno?
**R:** En docker-compose.yml:
```yaml
environment:
  USUARIO_SERVICE_URL: http://usuario-service:8001/usuario-service
```

O en .env (si lo creas):
```
USUARIO_SERVICE_URL=http://usuario-service:8001/usuario-service
```

---

## 🛠️ Troubleshooting

### Problema: "Connection refused"
```bash
# Solución
docker-compose down
docker-compose up -d
docker-compose logs -f
```

### Problema: "Port already in use"
```bash
# Encontrar qué usa el puerto 8001
lsof -i :8001

# Usar otro puerto en docker-compose.yml
ports:
  - "8001:8001"  # Cambiar a 8011:8001
```

### Problema: Base de datos no inicia
```bash
# Verificar estado
docker logs usuario-db

# Reiniciar
docker-compose restart usuario-db usuario-service
```

### Problema: Migraciones de BD fallan
```bash
# Ver estado de las tablas
docker exec usuario-db psql -U usuario -d usuarios_db -c "\dt"

# Limpiar (CUIDADO - BORRA DATOS)
docker-compose down -v
docker-compose up -d
```

---

## 📚 Recursos

- **Documentación oficial Spring Boot**: https://spring.io/projects/spring-boot
- **OpenAPI/Swagger**: https://swagger.io/
- **PostgreSQL**: https://www.postgresql.org/
- **Docker**: https://www.docker.com/

---

## 💡 Consejos

1. **Siempre revisar los logs** cuando algo no funciona
2. **Usar Swagger** para explorar las APIs interactivamente
3. **Crear usuarios antes de pedidos** (los pedidos validan usuarios)
4. **Respetar la estructura de datos** en las peticiones POST/PUT
5. **Usar `| jq .`** en cURL para ver JSON formateado

---

## 🎯 Próximos pasos

Después de entender estos conceptos, considera:

1. ✅ Agregar más microservicios (Productos, Pagos)
2. ✅ Implementar API Gateway (Kong, Zuul)
3. ✅ Agregar autenticación (JWT, OAuth2)
4. ✅ Implementar circuit breaker (Resilience4j)
5. ✅ Agregar caché (Redis)
6. ✅ Implementar logging centralizado (ELK)
7. ✅ Desplegar en Kubernetes
8. ✅ Agregar tests automatizados

---

¡Cualquier duda, revisa los logs y la documentación de Swagger! 🚀
