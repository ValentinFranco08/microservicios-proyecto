package com.microservices.pedido.repository;

import com.microservices.pedido.entity.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PedidoRepository Unit Tests")
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    private Pedido pedido1;
    private Pedido pedido2;

    @BeforeEach
    void setUp() {
        pedido1 = new Pedido();
        pedido1.setUsuarioId(1L);
        pedido1.setNumeroProducto("PROD-001");
        pedido1.setNombreProducto("Laptop");
        pedido1.setCantidad(1);
        pedido1.setPrecioUnitario(new BigDecimal("999.99"));
        pedido1.setPrecioTotal(new BigDecimal("999.99"));
        pedido1.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido1.setDireccionEnvio("Calle 123");
        pedidoRepository.save(pedido1);

        pedido2 = new Pedido();
        pedido2.setUsuarioId(1L);
        pedido2.setNumeroProducto("PROD-002");
        pedido2.setNombreProducto("Mouse");
        pedido2.setCantidad(2);
        pedido2.setPrecioUnitario(new BigDecimal("29.99"));
        pedido2.setPrecioTotal(new BigDecimal("59.98"));
        pedido2.setEstado(Pedido.EstadoPedido.CONFIRMADO);
        pedido2.setDireccionEnvio("Calle 123");
        pedidoRepository.save(pedido2);
    }

    @Test
    @DisplayName("Guardar pedido")
    void testSavePedido() {
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuarioId(2L);
        nuevoPedido.setNumeroProducto("PROD-003");
        nuevoPedido.setNombreProducto("Teclado");
        nuevoPedido.setCantidad(1);
        nuevoPedido.setPrecioUnitario(new BigDecimal("149.99"));
        nuevoPedido.setPrecioTotal(new BigDecimal("149.99"));
        nuevoPedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        nuevoPedido.setDireccionEnvio("Calle 456");

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        assertNotNull(pedidoGuardado.getId());
        assertEquals("Teclado", pedidoGuardado.getNombreProducto());
    }

    @Test
    @DisplayName("Obtener pedidos por usuario ID")
    void testFindByUsuarioId() {
        List<Pedido> resultado = pedidoRepository.findByUsuarioId(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Obtener pedidos por estado")
    void testFindByEstado() {
        List<Pedido> resultado = pedidoRepository.findByEstado(Pedido.EstadoPedido.PENDIENTE);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PROD-001", resultado.get(0).getNumeroProducto());
    }

    @Test
    @DisplayName("Obtener pedidos por usuario ID y estado")
    void testFindByUsuarioIdAndEstado() {
        List<Pedido> resultado = pedidoRepository.findByUsuarioIdAndEstado(1L, Pedido.EstadoPedido.CONFIRMADO);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PROD-002", resultado.get(0).getNumeroProducto());
    }

    @Test
    @DisplayName("Actualizar estado de pedido")
    void testUpdatePedidoEstado() {
        pedido1.setEstado(Pedido.EstadoPedido.EN_PROCESO);
        Pedido pedidoActualizado = pedidoRepository.save(pedido1);

        assertEquals(Pedido.EstadoPedido.EN_PROCESO, pedidoActualizado.getEstado());
    }

    @Test
    @DisplayName("Eliminar pedido")
    void testDeletePedido() {
        Long idPedido = pedido1.getId();
        pedidoRepository.deleteById(idPedido);

        assertFalse(pedidoRepository.existsById(idPedido));
    }
}
