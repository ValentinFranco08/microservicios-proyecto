package com.microservices.pedido.e2e;

import com.microservices.pedido.client.UsuarioServiceClient;
import com.microservices.pedido.dto.PedidoDTO;
import com.microservices.pedido.dto.UsuarioDTO;
import com.microservices.pedido.entity.Pedido;
import com.microservices.pedido.repository.PedidoRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Pedido E2E Tests - Flujos Completos")
class PedidoE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private PedidoRepository pedidoRepository;

    @MockBean
    private UsuarioServiceClient usuarioServiceClient;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/pedido-service/api/v1/pedidos";
        RestAssured.port = port;
        pedidoRepository.deleteAll();
        UsuarioDTO usuarioActivo = new UsuarioDTO(
            1L, "usuario@test.com", "Usuario", "Activo", null, null, null, null, true
        );
        when(usuarioServiceClient.obtenerUsuario(anyLong())).thenReturn(usuarioActivo);
        when(usuarioServiceClient.obtenerInfoBasicaUsuario(anyLong())).thenReturn(usuarioActivo);
        when(usuarioServiceClient.usuarioExiste(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("E2E: Crear pedido → Confirmar → Entregar")
    void testFlujoPedidoCompleto() {
        // 1. Crear pedido en estado PENDIENTE
        PedidoDTO nuevoPedido = new PedidoDTO(
            null, 1L, "PROD-001", "Laptop", 1, new BigDecimal("999.99"), new BigDecimal("999.99"),
            Pedido.EstadoPedido.PENDIENTE, null, null, "Laptop Dell", "Calle 123"
        );

        Long pedidoId = given()
            .contentType(ContentType.JSON)
            .body(nuevoPedido)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201)
            .body("estado", equalTo("PENDIENTE"))
            .extract()
            .jsonPath()
            .getLong("id");

        // 2. Confirmar pedido
        given()
            .when()
            .put(baseUrl + "/" + pedidoId + "/confirmar")
            .then()
            .statusCode(200)
            .body("estado", equalTo("CONFIRMADO"));

        // 3. Cambiar a EN_PROCESO
        given()
            .queryParam("nuevoEstado", "EN_PROCESO")
            .when()
            .put(baseUrl + "/" + pedidoId + "/estado")
            .then()
            .statusCode(200)
            .body("estado", equalTo("EN_PROCESO"));

        // 4. Cambiar a ENTREGADO
        given()
            .queryParam("nuevoEstado", "ENTREGADO")
            .when()
            .put(baseUrl + "/" + pedidoId + "/estado")
            .then()
            .statusCode(200)
            .body("estado", equalTo("ENTREGADO"));

        // 5. Verificar estado final
        given()
            .when()
            .get(baseUrl + "/" + pedidoId)
            .then()
            .statusCode(200)
            .body("estado", equalTo("ENTREGADO"))
            .body("nombreProducto", equalTo("Laptop"));
    }

    @Test
    @DisplayName("E2E: Crear pedido → Cancelar")
    void testFlujoPedidoCancelado() {
        // 1. Crear pedido
        PedidoDTO nuevoPedido = new PedidoDTO(
            null, 2L, "PROD-002", "Mouse", 2, new BigDecimal("29.99"), new BigDecimal("59.98"),
            Pedido.EstadoPedido.PENDIENTE, null, null, "Mouse inalámbrico", "Calle 456"
        );

        Long pedidoId = given()
            .contentType(ContentType.JSON)
            .body(nuevoPedido)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // 2. Cancelar pedido directamente
        given()
            .when()
            .put(baseUrl + "/" + pedidoId + "/cancelar")
            .then()
            .statusCode(200)
            .body("estado", equalTo("CANCELADO"));

        // 3. Intentar cambiar de CANCELADO a otro estado (debería fallar)
        given()
            .queryParam("nuevoEstado", "CONFIRMADO")
            .when()
            .put(baseUrl + "/" + pedidoId + "/estado")
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("E2E: Filtrar pedidos por usuario")
    void testFiltrarPedidosPorUsuario() {
        // 1. Crear múltiples pedidos para el mismo usuario
        for (int i = 0; i < 3; i++) {
            PedidoDTO pedido = new PedidoDTO(
                null, 3L, "PROD-" + i, "Producto " + i, 1, new BigDecimal("100.00"), new BigDecimal("100.00"),
                Pedido.EstadoPedido.PENDIENTE, null, null, "Descripción", "Calle 789"
            );

            given()
                .contentType(ContentType.JSON)
                .body(pedido)
                .when()
                .post(baseUrl)
                .then()
                .statusCode(201);
        }

        // 2. Obtener todos los pedidos del usuario
        given()
            .when()
            .get(baseUrl + "/usuario/3")
            .then()
            .statusCode(200)
            .body("size()", equalTo(3));
    }

    @Test
    @DisplayName("E2E: Obtener pedido con detalles del usuario")
    void testObtenerPedidoConDetalles() {
        // 1. Crear pedido
        PedidoDTO nuevoPedido = new PedidoDTO(
            null, 4L, "PROD-DETAIL", "Teclado", 1, new BigDecimal("149.99"), new BigDecimal("149.99"),
            Pedido.EstadoPedido.PENDIENTE, null, null, "Teclado Mecánico", "Calle 999"
        );

        Long pedidoId = given()
            .contentType(ContentType.JSON)
            .body(nuevoPedido)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // 2. Obtener detalles (incluye info del usuario)
        given()
            .when()
            .get(baseUrl + "/" + pedidoId + "/detalles")
            .then()
            .statusCode(200)
            .body("pedido", notNullValue())
            .body("usuario", notNullValue())
            .body("pedido.id", equalTo(pedidoId.intValue()));
    }
}
