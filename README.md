# Proyecto de Microservicios - Usuario, Pedido y Auth

Este proyecto implementa una arquitectura de microservicios con servicios independientes que se comunican entre sí:

- **Microservicio de Usuario**: Gestiona la información de usuarios
- **Microservicio de Pedido**: Gestiona los pedidos y se comunica con el servicio de Usuario
- **Microservicio de Auth**: Gestiona usuarios de autenticación y emite tokens JWT

## Características

- ✅ Spring Boot 3.1.5
- ✅ PostgreSQL como base de datos
- ✅ Docker y Docker Compose para contenerización
- ✅ Swagger/OpenAPI 3.0 para documentación de APIs
- ✅ Comunicación inter-microservicios mediante REST (RestTemplate)
- ✅ Autenticación con JWT para proteger el servicio de Pedido
- ✅ JPA/Hibernate para gestión de datos
- ✅ Health checks en Docker Compose
- ✅ Lombok para reducir código boilerplate

## Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                    Usuario Service                       │
│  - Gestiona información de usuarios                      │
│  - Base de datos PostgreSQL (puerto 5432)               │
│  - API en puerto interno 8001 / externo 18001             │
│  - Swagger: http://localhost:18001/usuario-service/      │
└──────────────────────┬──────────────────────────────────┘
                       │
                    REST API
                       │
                       ↓
┌─────────────────────────────────────────────────────────┐
│                    Auth Service                          │
│  - Crea usuarios de autenticación                        │
│  - Emite tokens JWT                                      │
│  - API en puerto interno 8003 / externo 18003             │
│  - Swagger: http://localhost:18003/auth-service/          │
└──────────────────────┬──────────────────────────────────┘
                       │
                    JWT Bearer
                       │
                       ↓
┌─────────────────────────────────────────────────────────┐
│                    Pedido Service                        │
│  - Gestiona pedidos                                      │
│  - Requiere token JWT para sus endpoints                 │
│  - Llama al servicio de Usuario para validaciones        │
│  - Base de datos PostgreSQL (puerto 5433)               │
│  - API en puerto interno 8002 / externo 18002             │
│  - Swagger: http://localhost:18002/pedido-service/       │
└─────────────────────────────────────────────────────────┘
```

## Requisitos previos

- Docker (v20.10+)
- Docker Compose (v1.29+)
- Git

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone <repo-url>
cd microservicios-proyecto
```

### 2. Construir y ejecutar los contenedores

```bash
docker-compose up -d
```

Este comando:
- Construye las imágenes Docker de los microservicios
- Crea las bases de datos PostgreSQL
- Inicia todos los servicios
- Espera a que los servicios estén listos

### 3. Verificar que los servicios están corriendo

```bash
docker-compose ps
```

Deberías ver:
```
NAME               STATUS      PORTS
usuario-db         Up          0.0.0.0:5432->5432/tcp
pedido-db          Up          0.0.0.0:5433->5432/tcp
usuario-service    Up          0.0.0.0:18001->8001/tcp
pedido-service     Up          0.0.0.0:18002->8002/tcp
auth-service       Up          0.0.0.0:18003->8003/tcp
```

## Acceso a las APIs

### Swagger Documentation

**Servicio de Usuario:**
- URL: http://localhost:18001/usuario-service/swagger-ui.html
- API Docs: http://localhost:18001/usuario-service/v3/api-docs

**Servicio de Pedido:**
- URL: http://localhost:18002/pedido-service/swagger-ui.html
- API Docs: http://localhost:18002/pedido-service/v3/api-docs

**Servicio de Auth:**
- URL: http://localhost:18003/auth-service/swagger-ui.html
- API Docs: http://localhost:18003/auth-service/v3/api-docs

## Ejemplos de Uso

### 1. Crear un Usuario

```bash
curl -X POST http://localhost:18001/usuario-service/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "telefono": "123456789",
    "direccion": "Calle Principal 123",
    "ciudad": "Buenos Aires",
    "pais": "Argentina"
  }'
```

Respuesta (ejemplo):
```json
{
  "id": 1,
  "email": "juan@example.com",
  "nombre": "Juan",
  "apellido": "Pérez",
  "telefono": "123456789",
  "direccion": "Calle Principal 123",
  "ciudad": "Buenos Aires",
  "pais": "Argentina",
  "activo": true
}
```

### 2. Obtener un Usuario

```bash
curl http://localhost:18001/usuario-service/api/v1/usuarios/1
```

### 3. Crear usuario de autenticación

```bash
curl -X POST http://localhost:18003/auth-service/create-user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "123456"
  }'
```

### 4. Iniciar sesión y obtener token

```bash
TOKEN=$(curl -s -X POST http://localhost:18003/auth-service/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "123456"
  }' | jq -r '.token')
```

### 5. Validar rechazo sin token o con token inválido

```bash
curl -i http://localhost:18002/pedido-service/api/v1/pedidos

curl -i http://localhost:18002/pedido-service/api/v1/pedidos \
  -H "Authorization: Bearer token-invalido"
```

Ambas llamadas deben responder `401 Unauthorized`.

### 6. Crear un Pedido (con token y validación de usuario)

```bash
curl -X POST http://localhost:18002/pedido-service/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "usuarioId": 1,
    "numeroProducto": "PROD-001",
    "nombreProducto": "Laptop",
    "cantidad": 1,
    "precioUnitario": 1000.00,
    "descripcion": "Laptop de gama alta",
    "direccionEnvio": "Calle Principal 123, Buenos Aires"
  }'
```

Respuesta (ejemplo):
```json
{
  "id": 1,
  "usuarioId": 1,
  "numeroProducto": "PROD-001",
  "nombreProducto": "Laptop",
  "cantidad": 1,
  "precioUnitario": 1000.00,
  "precioTotal": 1000.00,
  "estado": "PENDIENTE",
  "fechaCreacion": "2024-01-15T10:30:00",
  "fechaActualizacion": null,
  "descripcion": "Laptop de gama alta",
  "direccionEnvio": "Calle Principal 123, Buenos Aires"
}
```

### 7. Obtener Pedido con Detalles del Usuario

```bash
curl http://localhost:18002/pedido-service/api/v1/pedidos/1/detalles \
  -H "Authorization: Bearer $TOKEN"
```

Esta llamada demuestra la **comunicación inter-microservicios**:
- El servicio de Pedido consulta el servicio de Usuario automáticamente
- Retorna la información completa del pedido junto con los datos del usuario

### 8. Obtener Pedidos por Usuario

```bash
curl http://localhost:18002/pedido-service/api/v1/pedidos/usuario/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 9. Actualizar Estado de Pedido

```bash
curl -X PUT "http://localhost:18002/pedido-service/api/v1/pedidos/1/estado?nuevoEstado=CONFIRMADO" \
  -H "Authorization: Bearer $TOKEN"
```

Estados disponibles:
- PENDIENTE
- CONFIRMADO
- EN_PROCESO
- ENVIADO
- ENTREGADO
- CANCELADO

### 10. Confirmar Pedido

```bash
curl -X PUT http://localhost:18002/pedido-service/api/v1/pedidos/1/confirmar \
  -H "Authorization: Bearer $TOKEN"
```

## Endpoints principales

### Auth Service (Puerto 18003)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/create-user` | Crear usuario de autenticación en memoria |
| POST | `/login` | Iniciar sesión y obtener token JWT |

### Usuario Service (Puerto 18001)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/usuarios` | Obtener todos los usuarios |
| GET | `/api/v1/usuarios/{id}` | Obtener usuario por ID |
| GET | `/api/v1/usuarios/email/{email}` | Obtener usuario por email |
| POST | `/api/v1/usuarios` | Crear nuevo usuario |
| PUT | `/api/v1/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/v1/usuarios/{id}` | Eliminar usuario |
| GET | `/api/v1/usuarios/{id}/existe` | Verificar si existe usuario |

### Pedido Service (Puerto 18002, requiere JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/pedidos` | Obtener todos los pedidos |
| GET | `/api/v1/pedidos/{id}` | Obtener pedido por ID |
| GET | `/api/v1/pedidos/{id}/detalles` | Obtener pedido con datos del usuario |
| GET | `/api/v1/pedidos/usuario/{usuarioId}` | Obtener pedidos de un usuario |
| GET | `/api/v1/pedidos/estado/{estado}` | Obtener pedidos por estado |
| POST | `/api/v1/pedidos` | Crear nuevo pedido |
| PUT | `/api/v1/pedidos/{id}/estado` | Actualizar estado del pedido |
| PUT | `/api/v1/pedidos/{id}/confirmar` | Confirmar pedido |
| PUT | `/api/v1/pedidos/{id}/cancelar` | Cancelar pedido |

## Comunicación Inter-Microservicios

El Pedido Service se comunica con el Usuario Service de las siguientes formas:

1. **Validación al crear pedido**: Verifica que el usuario exista y esté activo
2. **Obtener información del usuario**: Recupera datos del usuario cuando se solicita un pedido con detalles
3. **Verificación de existencia**: Comprueba si un usuario existe antes de procesarlo

## Autenticación con JWT

El `auth-service` firma tokens con `JWT_SECRET`. El `pedido-service` usa el mismo secreto para validar cada request protegida. Los tokens deben enviarse con el encabezado:

```http
Authorization: Bearer <token>
```

El filtro de `pedido-service` rechaza requests sin token, con token inválido o con tokens que no contengan `sub`, `userId` y `roles`.

Ejemplo de cómo se implementa en código:

```java
// En PedidoService.crear()
UsuarioDTO usuario = usuarioServiceClient.obtenerUsuario(pedidoDTO.getUsuarioId());
if (!usuario.getActivo()) {
    throw new RuntimeException("No se puede crear pedido para usuario inactivo");
}
```

## Logs y debugging

Ver logs de un servicio específico:

```bash
# Usuario Service
docker logs usuario-service -f

# Pedido Service
docker logs pedido-service -f

# Auth Service
docker logs auth-service -f

# Base de datos Usuario
docker logs usuario-db -f
```

## Detener los servicios

```bash
docker-compose down
```

Para detener y eliminar volúmenes de datos:

```bash
docker-compose down -v
```

## Estructura del proyecto

```
microservicios-proyecto/
├── usuario-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/microservices/usuario/
│   │       │   ├── UsuarioServiceApplication.java
│   │       │   ├── controller/
│   │       │   ├── service/
│   │       │   ├── entity/
│   │       │   ├── dto/
│   │       │   └── repository/
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── pedido-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/microservices/pedido/
│   │       │   ├── PedidoServiceApplication.java
│   │       │   ├── controller/
│   │       │   ├── service/
│   │       │   ├── entity/
│   │       │   ├── dto/
│   │       │   ├── repository/
│   │       │   ├── client/
│   │       │   └── config/
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── auth-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/microservices/auth/
│   │       │   ├── AuthServiceApplication.java
│   │       │   ├── controller/
│   │       │   ├── service/
│   │       │   ├── dto/
│   │       │   └── config/
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml
└── README.md
```

## Tecnologías utilizadas

- **Spring Boot 3.1.5**: Framework principal
- **Spring Data JPA**: Acceso a datos
- **PostgreSQL**: Base de datos relacional
- **Docker & Docker Compose**: Contenerización
- **SpringDoc OpenAPI**: Documentación Swagger
- **Lombok**: Generación de código
- **RestTemplate**: Comunicación inter-servicios
- **JJWT**: Generación y validación de tokens JWT
- **Maven**: Gestor de dependencias

## Notas importantes

- Los servicios se comunican a través de Docker network llamada `microservices-network`
- Las bases de datos tienen usuarios y contraseñas por defecto (cambiar en producción)
- El secreto JWT está configurado para desarrollo en `docker-compose.yml`; cambiar en producción
- El health check verifica que los servicios estén listos antes de iniciar dependientes
- Las migraciones de base de datos se ejecutan automáticamente al iniciar (Hibernate ddl-auto: update)

## Próximas mejoras posibles

- Implementar API Gateway (Kong, Zuul)
- Agregar descubrimiento de servicios (Eureka, Consul)
- Implementar circuit breaker (Resilience4j)
- Agregar logging centralizado (ELK Stack)
- Implementar trazabilidad distribuida (Jaeger, Zipkin)
- Mejorar autorización por roles/permisos
- Implementar cache (Redis)
- Agregar unit tests y integration tests
- Configurar CI/CD (GitHub Actions, Jenkins)

## Soporte

Para reportar problemas o sugerencias, contactar a: soporte@microservicios.com
