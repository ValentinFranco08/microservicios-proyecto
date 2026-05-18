package com.microservices.usuario.e2e;

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

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Usuario E2E Tests - Flujos Completos")
class UsuarioE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/usuario-service/api/v1/usuarios";
        RestAssured.port = port;
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E: Crear usuario → Obtener → Actualizar → Eliminar")
    void testFlujoUsuarioCompleto() {
        // 1. Crear usuario
        UsuarioDTO nuevoUsuario = new UsuarioDTO(
            null, "Carlos", "López", "carlos@example.com", true, null, null,
            "1123456789", "Calle 123", "Buenos Aires", "Argentina"
        );

        Long usuarioId = given()
            .contentType(ContentType.JSON)
            .body(nuevoUsuario)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201)
            .body("nombre", equalTo("Carlos"))
            .extract()
            .jsonPath()
            .getLong("id");

        // 2. Obtener usuario por ID
        given()
            .when()
            .get(baseUrl + "/" + usuarioId)
            .then()
            .statusCode(200)
            .body("email", equalTo("carlos@example.com"));

        // 3. Actualizar usuario
        UsuarioDTO usuarioActualizado = new UsuarioDTO(
            usuarioId, "Carlos Actualizado", "López", "carlos@example.com", true, null, null,
            "1123456789", "Calle 456", "Buenos Aires", "Argentina"
        );

        given()
            .contentType(ContentType.JSON)
            .body(usuarioActualizado)
            .when()
            .put(baseUrl + "/" + usuarioId)
            .then()
            .statusCode(200)
            .body("nombre", equalTo("Carlos Actualizado"));

        // 4. Eliminar usuario
        given()
            .when()
            .delete(baseUrl + "/" + usuarioId)
            .then()
            .statusCode(204);

        // 5. Verificar que no existe
        given()
            .when()
            .get(baseUrl + "/" + usuarioId)
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("E2E: Crear usuario → Obtener por email")
    void testObtenerUsuarioPorEmail() {
        // 1. Crear usuario
        UsuarioDTO nuevoUsuario = new UsuarioDTO(
            null, "Ana", "García", "ana@example.com", true, null, null,
            "1198765432", "Avenida 456", "Buenos Aires", "Argentina"
        );

        given()
            .contentType(ContentType.JSON)
            .body(nuevoUsuario)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201);

        // 2. Obtener por email
        given()
            .when()
            .get(baseUrl + "/email/ana@example.com")
            .then()
            .statusCode(200)
            .body("nombre", equalTo("Ana"))
            .body("apellido", equalTo("García"));
    }

    @Test
    @DisplayName("E2E: Crear usuarios → Obtener todos")
    void testObtenerTodosLosUsuarios() {
        // 1. Crear múltiples usuarios
        for (int i = 1; i <= 3; i++) {
            UsuarioDTO usuario = new UsuarioDTO(
                null, "Usuario " + i, "Test", "usuario" + i + "@example.com", true, null, null,
                "110000000" + i, "Calle " + i, "Buenos Aires", "Argentina"
            );

            given()
                .contentType(ContentType.JSON)
                .body(usuario)
                .when()
                .post(baseUrl)
                .then()
                .statusCode(201);
        }

        // 2. Obtener todos
        given()
            .when()
            .get(baseUrl)
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(3));
    }

    @Test
    @DisplayName("E2E: Verificar si usuario existe")
    void testVerificarUsuarioExiste() {
        // 1. Crear usuario
        UsuarioDTO nuevoUsuario = new UsuarioDTO(
            null, "Roberto", "Sánchez", "roberto@example.com", true, null, null,
            "1111222233", "Calle 789", "Buenos Aires", "Argentina"
        );

        Long usuarioId = given()
            .contentType(ContentType.JSON)
            .body(nuevoUsuario)
            .when()
            .post(baseUrl)
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // 2. Verificar que existe
        given()
            .when()
            .get(baseUrl + "/" + usuarioId + "/existe")
            .then()
            .statusCode(200)
            .body(equalTo("true"));

        // 3. Verificar usuario no existe
        given()
            .when()
            .get(baseUrl + "/9999/existe")
            .then()
            .statusCode(200)
            .body(equalTo("false"));
    }
}
