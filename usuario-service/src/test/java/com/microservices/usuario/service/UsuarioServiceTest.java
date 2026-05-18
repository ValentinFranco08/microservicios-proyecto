package com.microservices.usuario.service;

import com.microservices.usuario.dto.UsuarioDTO;
import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService Unit Tests")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail("juan@example.com");
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        usuarioDTO = new UsuarioDTO(1L, "Juan", "Pérez", "juan@example.com", true, LocalDateTime.now(), null);
    }

    @Test
    @DisplayName("Obtener todos los usuarios")
    void testObtenerTodos() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener usuario por ID")
    void testObtenerPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener usuario por ID no encontrado")
    void testObtenerPorIdNoEncontrado() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usuarioService.obtenerPorId(999L));
        verify(usuarioRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Obtener usuario por email")
    void testObtenerPorEmail() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.obtenerPorEmail("juan@example.com");

        assertNotNull(resultado);
        assertEquals("juan@example.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findByEmail("juan@example.com");
    }

    @Test
    @DisplayName("Crear nuevo usuario")
    void testCrearUsuario() {
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioDTO resultado = usuarioService.crear(usuarioDTO);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Actualizar usuario")
    void testActualizarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioDTO.setNombre("Juan Actualizado");
        UsuarioDTO resultado = usuarioService.actualizar(1L, usuarioDTO);

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Eliminar usuario")
    void testEliminarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        usuarioService.eliminar(1L);

        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Usuario existe")
    void testUsuarioExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        boolean resultado = usuarioService.existeUsuario(1L);

        assertTrue(resultado);
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Usuario no existe")
    void testUsuarioNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        boolean resultado = usuarioService.existeUsuario(999L);

        assertFalse(resultado);
        verify(usuarioRepository, times(1)).findById(999L);
    }
}
