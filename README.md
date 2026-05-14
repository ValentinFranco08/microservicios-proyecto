# Proyecto de Microservicios - Usuario y Pedido

Este proyecto implementa una arquitectura de microservicios con dos servicios independientes que se comunican entre sí:

- **Microservicio de Usuario**: Gestiona la información de usuarios
- **Microservicio de Pedido**: Gestiona los pedidos y se comunica con el servicio de Usuario

## Características

- ✅ Spring Boot 3.1.5
- ✅ PostgreSQL como base de datos
- ✅ Docker y Docker Compose para contenerización
- ✅ Swagger/OpenAPI 3.0 para documentación de APIs
- ✅ Comunicación inter-microservicios mediante REST (RestTemplate)
- ✅ JPA/Hibernate para gestión de datos
- ✅ Health checks en Docker Compose
- ✅ Lombok para reducir código boilerplate

## Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                    Usuario Service                       │
│  - Gestiona información de usuarios                      │
│  - Base de datos PostgreSQL (puerto 5432)               │
│  - API en puerto 8001                                    │
│  - Swagger: http://localhost:8001/usuario-service/      │
└──────────────────────┬──────────────────────────────────┘
                       │
                    REST API
                       │
                       ↓
┌─────────────────────────────────────────────────────────┐
│                    Pedido Service                        │
│  - Gestiona pedidos                                      │
│  - Llama al servicio de Usuario para validaciones      │
│  - Base de datos PostgreSQL (puerto 5433)               │
│  - API en puerto 8002                                    │
│  - Swagger: http://localhost:8002/pedido-service/       │
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
- Construye las imágenes Docker de ambos microservicios
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
usuario-service    Up          0.0.0.0:8001->8001/tcp
pedido-service     Up          0.0.0.0:8002->8002/tcp
```

## Acceso a las APIs

### Swagger Documentation

**Servicio de Usuario:**
- URL: http://localhost:8001/usuario-service/swagger-ui.html
- API Docs: http://localhost:8001/usuario-service/v3/api-docs

**Servicio de Pedido:**
- URL: http://localhost:8002/pedido-service/swagger-ui.html
- API Docs: http://localhost:8002/pedido-service/v3/api-docs

## Ejemplos de Uso

### 1. Crear un Usuario

```bash
curl -X POST http://localhost:8001/usuario-service/api/v1/usuarios \
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
curl http://localhost:8001/usuario-service/api/v1/usuarios/1
```

### 3. Crear un Pedido (con validación de usuario)

```bash
curl -X POST http://localhost:8002/pedido-service/api/v1/pedidos \
  -H "Content-Type: application/json" \
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

### 4. Obtener Pedido con Detalles del Usuario

```bash
curl http://localhost:8002/pedido-service/api/v1/pedidos/1/detalles
```

Esta llamada demuestra la **comunicación inter-microservicios**:
- El servicio de Pedido consulta el servicio de Usuario automáticamente
- Retorna la información completa del pedido junto con los datos del usuario

### 5. Obtener Pedidos por Usuario

```bash
curl http://localhost:8002/pedido-service/api/v1/pedidos/usuario/1
```

### 6. Actualizar Estado de Pedido

```bash
curl -X PUT "http://localhost:8002/pedido-service/api/v1/pedidos/1/estado?nuevoEstado=CONFIRMADO"
```

Estados disponibles:
- PENDIENTE
- CONFIRMADO
- EN_PROCESO
- ENVIADO
- ENTREGADO
- CANCELADO

### 7. Confirmar Pedido

```bash
curl -X PUT http://localhost:8002/pedido-service/api/v1/pedidos/1/confirmar
```

## Endpoints principales

### Usuario Service (Puerto 8001)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/usuarios` | Obtener todos los usuarios |
| GET | `/api/v1/usuarios/{id}` | Obtener usuario por ID |
| GET | `/api/v1/usuarios/email/{email}` | Obtener usuario por email |
| POST | `/api/v1/usuarios` | Crear nuevo usuario |
| PUT | `/api/v1/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/v1/usuarios/{id}` | Eliminar usuario |
| GET | `/api/v1/usuarios/{id}/existe` | Verificar si existe usuario |

### Pedido Service (Puerto 8002)

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
- **Maven**: Gestor de dependencias

## Notas importantes

- Los servicios se comunican a través de Docker network llamada `microservices-network`
- Las bases de datos tienen usuarios y contraseñas por defecto (cambiar en producción)
- El health check verifica que los servicios estén listos antes de iniciar dependientes
- Las migraciones de base de datos se ejecutan automáticamente al iniciar (Hibernate ddl-auto: update)

## Próximas mejoras posibles

- Implementar API Gateway (Kong, Zuul)
- Agregar descubrimiento de servicios (Eureka, Consul)
- Implementar circuit breaker (Resilience4j)
- Agregar logging centralizado (ELK Stack)
- Implementar trazabilidad distribuida (Jaeger, Zipkin)
- Agregar autenticación y autorización (OAuth2, JWT)
- Implementar cache (Redis)
- Agregar unit tests y integration tests
- Configurar CI/CD (GitHub Actions, Jenkins)

## Soporte

Para reportar problemas o sugerencias, contactar a: soporte@microservicios.com
