# Documentación de Arquitectura - Microservicios Usuario y Pedido

## 📋 Índice
1. [Introducción](#introducción)
2. [Arquitectura General](#arquitectura-general)
3. [Comunicación Inter-Microservicios](#comunicación-inter-microservicios)
4. [Contrato de API (OpenAPI/Swagger)](#contrato-de-api)
5. [Escalabilidad](#escalabilidad)
6. [Deployment](#deployment)

---

## Introducción

Este proyecto implementa una arquitectura de microservicios moderna con dos servicios independientes que se comunican a través de APIs REST:

- **Usuario Service**: Gestiona el ciclo de vida de los usuarios
- **Pedido Service**: Gestiona los pedidos y depende del Usuario Service para validaciones

### Principios de Microservicios aplicados

✅ **Independencia**: Cada servicio tiene su propia base de datos
✅ **Escalabilidad**: Cada servicio se puede escalar independientemente
✅ **Flexibilidad**: Permite usar diferentes tecnologías en cada servicio
✅ **Resiliencia**: Un servicio puede fallar sin derribar los demás
✅ **Documentación**: APIs documentadas con OpenAPI/Swagger

---

## Arquitectura General

### Vista de alto nivel

```
┌──────────────────────────────────────────────────────────────────┐
│                        Cliente/Frontend                          │
└────────────────┬───────────────────────┬────────────────────────┘
                 │                       │
            REST API                 REST API
                 │                       │
    ┌────────────▼──────────┐  ┌────────▼──────────────┐
    │ Usuario Service       │  │ Pedido Service        │
    │ ┌──────────────────┐  │  │ ┌──────────────────┐  │
    │ │ UsuarioController│  │  │ │PedidoController │  │
    │ ├──────────────────┤  │  │ ├──────────────────┤  │
    │ │ UsuarioService   │  │  │ │PedidoService     │  │
    │ ├──────────────────┤  │  │ ├──────────────────┤  │
    │ │ UsuarioRepository│  │  │ │PedidoRepository  │  │
    │ └──────────────────┘  │  │ └──────────────────┘  │
    │        │              │  │        │               │
    │ ┌──────▼──────────┐  │  │ ┌──────▼────────────┐ │
    │ │ PostgreSQL (1)  │  │  │ │ PostgreSQL (2)   │ │
    │ └─────────────────┘  │  │ └──────────────────┘ │
    └─────────────────────┘  └───────────┬───────────┘
                                         │
                                    REST API
                                         │
                                 ┌───────▼───────┐
                                 │ Usuario Service│
                                 └────────────────┘
```

### Componentes

1. **Cliente/Frontend**: Aplicación web o móvil que consume las APIs
2. **Gateway** (Opcional): Punto de entrada único para todas las solicitudes
3. **Microservicios**: Servicios independientes con responsabilidades específicas
4. **Bases de Datos**: Cada servicio tiene su propia base de datos (NoSQL o Relacional)
5. **Redes**: Docker network para comunicación inter-contenedores

---

## Comunicación Inter-Microservicios

### ¿Por qué se comunican?

El Pedido Service necesita validar que un usuario existe antes de crear un pedido. Esta es una comunicación sincrónica (síncrona) Backend-to-Backend.

### Flujo de creación de pedido

```
1. Cliente → POST /api/v1/pedidos
            ├─ usuarioId: 1
            ├─ nombreProducto: "Laptop"
            └─ cantidad: 1

2. PedidoService recibe la solicitud
   ├─ Valida los parámetros
   └─ Llama a UsuarioService para verificar usuario

3. RestTemplate (Cliente HTTP)
   └─ GET http://usuario-service:8001/usuario-service/api/v1/usuarios/1

4. UsuarioService procesa la solicitud
   ├─ Busca usuario en BD
   └─ Retorna datos del usuario

5. PedidoService verifica respuesta
   ├─ ¿Usuario existe? ✓
   ├─ ¿Usuario está activo? ✓
   └─ Procede a crear el pedido

6. Se guarda el pedido en BD

7. Se retorna al cliente
   └─ {"id": 1, "estado": "PENDIENTE", ...}
```

### Código de ejemplo

**En PedidoService.crear():**

```java
public PedidoDTO crear(PedidoDTO pedidoDTO) {
    // COMUNICACIÓN INTER-MICROSERVICIOS
    log.info("Verificando usuario con ID: {}", pedidoDTO.getUsuarioId());
    
    // Llamada HTTP al servicio de Usuario
    UsuarioDTO usuario = usuarioServiceClient.obtenerUsuario(
        pedidoDTO.getUsuarioId()
    );
    
    // Validar respuesta
    if (!usuario.getActivo()) {
        throw new RuntimeException("Usuario inactivo");
    }

    log.info("Usuario validado: {}", usuario.getNombre());

    // Crear pedido
    Pedido pedido = new Pedido();
    pedido.setUsuarioId(pedidoDTO.getUsuarioId());
    // ... más campos
    
    return convertirADTO(pedidoRepository.save(pedido));
}
```

**En UsuarioServiceClient:**

```java
@Component
public class UsuarioServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${usuario.service.url}")
    private String usuarioServiceUrl;

    public UsuarioDTO obtenerUsuario(Long usuarioId) {
        String url = usuarioServiceUrl + "/api/v1/usuarios/" + usuarioId;
        return restTemplate.getForObject(url, UsuarioDTO.class);
    }
}
```

### Tipos de comunicación implementados

1. **Backend → Backend (HTTP REST)**
   - Pedido Service → Usuario Service
   - Método: RestTemplate
   - Sincrónico (espera respuesta)

### Otros tipos de comunicación (no implementados aquí)

- **Message Queue** (Asincrónico): RabbitMQ, Kafka
- **gRPC** (Alto rendimiento): Protocol Buffers
- **GraphQL** (Flexible): Consultas específicas
- **Service Mesh**: Istio, Linkerd

---

## Contrato de API

### OpenAPI/Swagger

Cada servicio documenta sus APIs usando OpenAPI 3.0, lo que permite:

✅ Documentación automática e interactiva
✅ Generación de clientes (SDK)
✅ Testing de endpoints
✅ Versionamiento de APIs

### Configuración de Swagger

**En UsuarioServiceApplication.java:**

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Usuario Service API")
            .version("1.0.0")
            .description("API REST para gestión de usuarios"));
}
```

### Anotaciones en controladores

```java
@GetMapping("/{id}")
@Operation(summary = "Obtener usuario por ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
})
public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
    return ResponseEntity.ok(usuarioService.obtenerPorId(id));
}
```

### Accediendo a Swagger

```
http://localhost:8001/usuario-service/swagger-ui.html
http://localhost:8002/pedido-service/swagger-ui.html
```

---

## Escalabilidad

### Escalabilidad Horizontal

Cada servicio se puede escalar independientemente:

```bash
# Escalar Usuario Service a 3 instancias
docker-compose up -d --scale usuario-service=3

# Escalar Pedido Service a 2 instancias
docker-compose up -d --scale pedido-service=2
```

### Ventajas

- Usuario Service maneja muchas solicitudes → escala arriba
- Pedido Service es más eficiente → usa menos recursos
- Cada servicio se optimiza según su demanda real

### Consideraciones

1. **Load Balancer**: Necesario para distribuir tráfico entre instancias
2. **Sticky Sessions**: Si las mantienes, distribuye por afinidad
3. **Base de datos**: Debe poder manejar múltiples conexiones
4. **Caché**: Considera Redis para compartir estado

### Flujo con múltiples instancias

```
Cliente
   │
   ├─ (Round Robin)
   │
   ├──→ Usuario Service #1
   ├──→ Usuario Service #2
   └──→ Usuario Service #3
   
   ├──→ Pedido Service #1
   └──→ Pedido Service #2
   
   Ambos comparten misma BD
```

---

## Deployment

### Ambientes

#### Desarrollo
```yaml
docker-compose up -d
```

#### Staging/Producción
```bash
# Usar image registry (DockerHub, ECR, etc)
docker build -t myregistry/usuario-service:1.0.0 .
docker push myregistry/usuario-service:1.0.0

# Usar Kubernetes
kubectl apply -f usuario-deployment.yaml
kubectl apply -f pedido-deployment.yaml
```

### Kubernetes (Opcional)

**usuario-deployment.yaml**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: usuario-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: usuario-service
  template:
    metadata:
      labels:
        app: usuario-service
    spec:
      containers:
      - name: usuario-service
        image: myregistry/usuario-service:1.0.0
        ports:
        - containerPort: 8001
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/usuarios_db
```

---

## Variables de Entorno

### Pedido Service

```bash
USUARIO_SERVICE_URL=http://usuario-service:8001/usuario-service
SPRING_DATASOURCE_URL=jdbc:postgresql://pedido-db:5432/pedidos_db
SPRING_DATASOURCE_USERNAME=pedido
SPRING_DATASOURCE_PASSWORD=pedido123
```

### Usuario Service

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://usuario-db:5432/usuarios_db
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=usuario123
```

---

## Health Checks

Docker Compose incluye health checks para verificar que los servicios estén listos:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8001/usuario-service/api/v1/usuarios"]
  interval: 10s
  timeout: 5s
  retries: 10
```

---

## Monitoreo

### Logs

```bash
# Ver logs en tiempo real
docker-compose logs -f usuario-service
docker-compose logs -f pedido-service

# Ver logs de los últimos 100 líneas
docker-compose logs --tail=100 usuario-service
```

### Métricas (Opcional - Spring Boot Actuator)

Agregar a pom.xml:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Acceder a métricas:
```
http://localhost:8001/usuario-service/actuator
http://localhost:8002/pedido-service/actuator
```

---

## Troubleshooting

### Servicio no se inicia

```bash
# Ver logs
docker logs usuario-service

# Verificar conectividad a BD
docker exec usuario-service ping usuario-db

# Reiniciar servicio
docker-compose restart usuario-service
```

### Error: "Usuario Service no responde"

```bash
# Verificar que el servicio está en ejecución
docker ps | grep usuario-service

# Verificar logs
docker logs usuario-service

# Verificar conectividad
docker exec pedido-service curl http://usuario-service:8001/usuario-service/api/v1/usuarios
```

### Error: "Database connection refused"

```bash
# Verificar estado de BD
docker logs usuario-db

# Reconectar
docker-compose restart usuario-db usuario-service
```

---

## Conclusión

Este proyecto demuestra:

✅ Cómo crear microservicios independientes
✅ Cómo documentar APIs con Swagger
✅ Cómo implementar comunicación inter-servicios
✅ Cómo contenerizar aplicaciones con Docker
✅ Cómo escalar servicios horizontalmente
✅ Cómo preparar para producción

Para más información, consulta el README.md principal.
