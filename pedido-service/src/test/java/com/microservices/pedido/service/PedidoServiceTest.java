package com.microservices.pedido.service;

import com.microservices.pedido.client.UsuarioServiceClient;
import com.microservices.pedido.dto.PedidoDTO;
import com.microservices.pedido.dto.UsuarioDTO;
import com.microservices.pedido.entity.Pedido;
import com.microservices.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService Unit Tests")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioServiceClient usuarioServiceClient;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private PedidoDTO pedidoDTO;
    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuarioId(1L);
        pedido.setNumeroProducto("PROD-001");
        pedido.setNombreProducto("Laptop");
        pedido.setCantidad(1);
        pedido.setPrecioUnitario(new BigDecimal("999.99"));
        pedido.setPrecioTotal(new BigDecimal("999.99"));
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setDireccionEnvio("Calle 123");

        pedidoDTO = new PedidoDTO(1L, 1L, "PROD-001", "Laptop", 1, 
            new BigDecimal("999.99"), new BigDecimal("999.99"), 
            Pedido.EstadoPedido.PENDIENTE, LocalDateTime.now(), null, "Descripción", "Calle 123");

        usuarioDTO = new UsuarioDTO(1L, "juan@example.com", "Juan", "Pérez", null, null, null, null, true);
    }

    @Test
    @DisplayName("Obtener todos los pedidos")
    void testObtenerTodos() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        List<PedidoDTO> resultado = pedidoService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener pedido por ID")
    void testObtenerPorId() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoDTO resultado = pedidoService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener pedido por usuario")
    void testObtenerPorUsuario() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(usuarioServiceClient.usuarioExiste(1L)).thenReturn(true);
        when(pedidoRepository.findByUsuarioId(1L)).thenReturn(pedidos);

        List<PedidoDTO> resultado = pedidoService.obtenerPorUsuario(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    @DisplayName("Crear pedido exitosamente")
    void testCrearPedido() {
        when(usuarioServiceClient.obtenerUsuario(1L)).thenReturn(usuarioDTO);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        PedidoDTO resultado = pedidoService.crear(pedidoDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(usuarioServiceClient, times(1)).obtenerUsuario(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Crear pedido con usuario inactivo")
    void testCrearPedidoUsuarioInactivo() {
        usuarioDTO.setActivo(false);
        when(usuarioServiceClient.obtenerUsuario(1L)).thenReturn(usuarioDTO);

        assertThrows(RuntimeException.class, () -> pedidoService.crear(pedidoDTO));
        verify(usuarioServiceClient, times(1)).obtenerUsuario(1L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Actualizar estado de pedido")
    void testActualizarEstado() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        PedidoDTO resultado = pedidoService.actualizarEstado(1L, Pedido.EstadoPedido.CONFIRMADO);

        assertNotNull(resultado);
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Confirmar pedido")
    void testConfirmarPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        PedidoDTO resultado = pedidoService.confirmar(1L);

        assertNotNull(resultado);
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Cancelar pedido")
    void testCancelarPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        PedidoDTO resultado = pedidoService.cancelar(1L);

        assertNotNull(resultado);
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener pedido con detalles")
    void testObtenerPedidoConDetalles() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(usuarioServiceClient.obtenerInfoBasicaUsuario(1L)).thenReturn(usuarioDTO);

        PedidoService.PedidoConDetallesDTO resultado = pedidoService.obtenerPedidoConDetalles(1L);

        assertNotNull(resultado);
        assertNotNull(resultado.getPedido());
        assertNotNull(resultado.getUsuario());
        assertEquals("Juan", resultado.getUsuario().getNombre());
        verify(pedidoRepository, times(1)).findById(1L);
        verify(usuarioServiceClient, times(1)).obtenerInfoBasicaUsuario(1L);
    }
}
