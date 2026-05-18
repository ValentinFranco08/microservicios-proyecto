package com.microservices.pedido.controller;

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
import java.time.LocalDateTime;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("PedidoController Integration Tests")
class PedidoControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PedidoRepository pedidoRepository;

    @MockBean
    private UsuarioServiceClient usuarioServiceClient;

    private String baseUrl;
    private Pedido pedido;

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

        pedido = new Pedido();
        pedido.setUsuarioId(1L);
        pedido.setNumeroProducto("PROD-001");
        pedido.setNombreProducto("Laptop");
        pedido.setCantidad(1);
        pedido.setPrecioUnitario(new BigDecimal("999.99"));
        pedido.setPrecioTotal(new BigDecimal("999.99"));
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setDireccionEnvio("Calle 123");
        pedido = pedidoRepository.save(pedido);
    }

    @Test
    @DisplayName("GET /pedidos - Obtener todos los pedidos")
    void testObtenerTodos() {
        given()
            .when()
            .get(baseUrl)
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("GET /pedidos/{id} - Obtener pedido por ID")
    void testObtenerPorId() {
        given()
            .when()
            .get(baseUrl + "/" + pedido.getId())
            .then()
            .statusCode(200)
            .body("id", equalTo(pedido.getId().intValue()))
            .body("nombreProducto", equalTo("Laptop"));
    }

    @Test
    @DisplayName("GET /pedidos/{id}/detalles - Obtener detalles del pedido")
    void testObtenerDetalles() {
        given()
            .when()
            .get(baseUrl + "/" + pedido.getId() + "/detalles")
            .then()
            .statusCode(200)
            .body("pedido", notNullValue())
            .body("usuario", notNullValue());
    }

    @Test
    @DisplayName("GET /pedidos/usuario/{usuarioId} - Obtener pedidos por usuario")
    void testObtenerPorUsuario() {
        given()
            .when()
            .get(baseUrl + "/usuario/1")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("GET /pedidos/estado/{estado} - Obtener pedidos por estado")
    void testObtenerPorEstado() {
        given()
            .when()
            .get(baseUrl + "/estado/PENDIENTE")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("POST /pedidos - Crear nuevo pedido")
    void testCrearPedido() {
        PedidoDTO nuevoPedido = new PedidoDTO(
            null, 1L, "PROD-002", "Mouse", 2, new BigDecimal("29.99"), new BigDecimal("59.98"),
            Pedido.EstadoPedido.PENDIENTE, null, null, "Mouse inalámbrico", "Calle 123"
        );

        given()
            .contentType(ContentType.JSON)
            .body(nuevoPedido)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201)
            .body("nombreProducto", equalTo("Mouse"));
    }

    @Test
    @DisplayName("PUT /pedidos/{id}/estado - Actualizar estado del pedido")
    void testActualizarEstado() {
        given()
            .queryParam("nuevoEstado", "CONFIRMADO")
            .when()
            .put(baseUrl + "/" + pedido.getId() + "/estado")
            .then()
            .statusCode(200)
            .body("estado", equalTo("CONFIRMADO"));
    }

    @Test
    @DisplayName("PUT /pedidos/{id}/confirmar - Confirmar pedido")
    void testConfirmarPedido() {
        given()
            .when()
            .put(baseUrl + "/" + pedido.getId() + "/confirmar")
            .then()
            .statusCode(200)
            .body("estado", equalTo("CONFIRMADO"));
    }

    @Test
    @DisplayName("PUT /pedidos/{id}/cancelar - Cancelar pedido")
    void testCancelarPedido() {
        given()
            .when()
            .put(baseUrl + "/" + pedido.getId() + "/cancelar")
            .then()
            .statusCode(200)
            .body("estado", equalTo("CANCELADO"));
    }
}
