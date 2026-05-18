package com.microservices.pedido.client;

import com.microservices.pedido.dto.UsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceClient Unit Tests")
class UsuarioServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UsuarioServiceClient usuarioServiceClient;

    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        usuarioDTO = new UsuarioDTO(1L, "juan@example.com", "Juan", "Pérez", null, null, null, null, true);
    }

    @Test
    @DisplayName("Obtener usuario exitosamente")
    void testObtenerUsuario() {
        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class)))
            .thenReturn(usuarioDTO);

        UsuarioDTO resultado = usuarioServiceClient.obtenerUsuario(1L);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(UsuarioDTO.class));
    }

    @Test
    @DisplayName("Usuario existe")
    void testUsuarioExiste() {
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenReturn(true);

        boolean resultado = usuarioServiceClient.usuarioExiste(1L);

        assertTrue(resultado);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Boolean.class));
    }

    @Test
    @DisplayName("Usuario no existe")
    void testUsuarioNoExiste() {
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenReturn(false);

        boolean resultado = usuarioServiceClient.usuarioExiste(999L);

        assertFalse(resultado);
    }

    @Test
    @DisplayName("Obtener información básica del usuario")
    void testObtenerInfoBasicaUsuario() {
        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class)))
            .thenReturn(usuarioDTO);

        UsuarioDTO resultado = usuarioServiceClient.obtenerInfoBasicaUsuario(1L);

        assertNotNull(resultado);
        assertEquals("juan@example.com", resultado.getEmail());
    }
}
