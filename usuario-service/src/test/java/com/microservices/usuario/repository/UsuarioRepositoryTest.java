package com.microservices.usuario.repository;

import com.microservices.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UsuarioRepository Unit Tests")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail("juan@example.com");
        usuario.setTelefono("1123456789");
        usuario.setDireccion("Calle 123");
        usuario.setCiudad("Buenos Aires");
        usuario.setPais("Argentina");
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("Guardar usuario")
    void testSaveUsuario() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("María");
        nuevoUsuario.setApellido("García");
        nuevoUsuario.setEmail("maria@example.com");
        nuevoUsuario.setTelefono("1198765432");
        nuevoUsuario.setDireccion("Avenida 456");
        nuevoUsuario.setCiudad("Buenos Aires");
        nuevoUsuario.setPais("Argentina");
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        assertNotNull(usuarioGuardado.getId());
        assertEquals("María", usuarioGuardado.getNombre());
    }

    @Test
    @DisplayName("Buscar usuario por email")
    void testFindByEmail() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("juan@example.com");

        assertTrue(resultado.isPresent());
        assertEquals("Juan", resultado.get().getNombre());
    }

    @Test
    @DisplayName("Buscar usuario por email no existente")
    void testFindByEmailNotFound() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("noexiste@example.com");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Actualizar usuario")
    void testUpdateUsuario() {
        usuario.setNombre("Juan Actualizado");
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        assertEquals("Juan Actualizado", usuarioActualizado.getNombre());
    }

    @Test
    @DisplayName("Eliminar usuario")
    void testDeleteUsuario() {
        Long idUsuario = usuario.getId();
        usuarioRepository.deleteById(idUsuario);

        Optional<Usuario> resultado = usuarioRepository.findById(idUsuario);
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Verificar usuario existe")
    void testExistsByEmail() {
        boolean existe = usuarioRepository.existsByEmail("juan@example.com");
        assertTrue(existe);
    }

    @Test
    @DisplayName("Usuario no existe")
    void testNotExistsByEmail() {
        boolean existe = usuarioRepository.existsByEmail("noexiste@example.com");
        assertFalse(existe);
    }
}
