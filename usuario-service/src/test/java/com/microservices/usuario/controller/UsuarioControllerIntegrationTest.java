package com.microservices.usuario.controller;

import com.microservices.usuario.dto.UsuarioDTO;
import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.repository.UsuarioRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("UsuarioController Integration Tests")
class UsuarioControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String baseUrl;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/usuario-service/api/v1/usuarios";
        RestAssured.port = port;
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNombre("Test Usuario");
        usuario.setApellido("Test");
        usuario.setEmail("test@example.com");
        usuario.setTelefono("1123456789");
        usuario.setDireccion("Calle Test 123");
        usuario.setCiudad("Buenos Aires");
        usuario.setPais("Argentina");
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("GET /usuarios - Obtener todos los usuarios")
    void testObtenerTodos() {
        given()
            .when()
            .get(baseUrl)
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Obtener usuario por ID")
    void testObtenerPorId() {
        given()
            .when()
            .get(baseUrl + "/" + usuario.getId())
            .then()
            .statusCode(200)
            .body("id", equalTo(usuario.getId().intValue()))
            .body("nombre", equalTo("Test Usuario"));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Usuario no encontrado")
    void testObtenerPorIdNoEncontrado() {
        given()
            .when()
            .get(baseUrl + "/999")
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET /usuarios/email/{email} - Obtener usuario por email")
    void testObtenerPorEmail() {
        given()
            .when()
            .get(baseUrl + "/email/test@example.com")
            .then()
            .statusCode(200)
            .body("email", equalTo("test@example.com"));
    }

    @Test
    @DisplayName("POST /usuarios - Crear nuevo usuario")
    void testCrearUsuario() {
        UsuarioDTO nuevoUsuario = new UsuarioDTO(
            null, "Nuevo", "Usuario", "nuevo@example.com", true, null, null,
            "1198765432", "Avenida Nueva 456", "Buenos Aires", "Argentina"
        );

        given()
            .contentType(ContentType.JSON)
            .body(nuevoUsuario)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201)
            .body("nombre", equalTo("Nuevo"))
            .body("email", equalTo("nuevo@example.com"));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Actualizar usuario")
    void testActualizarUsuario() {
        UsuarioDTO usuarioActualizado = new UsuarioDTO(
            usuario.getId(), "Juan Actualizado", "Pérez", "juan@example.com", true, null, null,
            "1111222233", "Calle Actualizada 789", "La Plata", "Argentina"
        );

        given()
            .contentType(ContentType.JSON)
            .body(usuarioActualizado)
            .when()
            .put(baseUrl + "/" + usuario.getId())
            .then()
            .statusCode(200)
            .body("nombre", equalTo("Juan Actualizado"));
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} - Eliminar usuario")
    void testEliminarUsuario() {
        given()
            .when()
            .delete(baseUrl + "/" + usuario.getId())
            .then()
            .statusCode(204);

        given()
            .when()
            .get(baseUrl + "/" + usuario.getId())
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET /usuarios/{id}/existe - Verificar si usuario existe")
    void testUsuarioExiste() {
        given()
            .when()
            .get(baseUrl + "/" + usuario.getId() + "/existe")
            .then()
            .statusCode(200)
            .body(equalTo("true"));
    }

    @Test
    @DisplayName("GET /usuarios/{id}/info-basica - Obtener información básica del usuario")
    void testObtenerInfoBasica() {
        given()
            .when()
            .get(baseUrl + "/" + usuario.getId() + "/info-basica")
            .then()
            .statusCode(200)
            .body("nombre", equalTo("Test Usuario"));
    }
}
