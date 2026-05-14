package com.microservices.pedido.service;

import com.microservices.pedido.client.UsuarioServiceClient;
import com.microservices.pedido.dto.PedidoDTO;
import com.microservices.pedido.dto.UsuarioDTO;
import com.microservices.pedido.entity.Pedido;
import com.microservices.pedido.entity.Pedido.EstadoPedido;
import com.microservices.pedido.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioServiceClient usuarioServiceClient;

    /**
     * Obtener todos los pedidos
     */
    public List<PedidoDTO> obtenerTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pedido por ID
     */
    public PedidoDTO obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Obtener pedidos por usuario
     */
    public List<PedidoDTO> obtenerPorUsuario(Long usuarioId) {
        // Verificar que el usuario existe
        if (!usuarioServiceClient.usuarioExiste(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }

        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pedidos por estado
     */
    public List<PedidoDTO> obtenerPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crear nuevo pedido (valida usuario antes de crear)
     */
    public PedidoDTO crear(PedidoDTO pedidoDTO) {
        // COMUNICACIÓN INTER-SERVICIOS: Verificar que el usuario existe
        log.info("Verificando usuario con ID: {}", pedidoDTO.getUsuarioId());
        UsuarioDTO usuario = usuarioServiceClient.obtenerUsuario(pedidoDTO.getUsuarioId());
        
        if (!usuario.getActivo()) {
            throw new RuntimeException("No se puede crear pedido para usuario inactivo");
        }

        log.info("Usuario validado: {} {}", usuario.getNombre(), usuario.getApellido());

        // Calcular precio total
        BigDecimal precioTotal = pedidoDTO.getPrecioUnitario()
                .multiply(new BigDecimal(pedidoDTO.getCantidad()));

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(pedidoDTO.getUsuarioId());
        pedido.setNumeroProducto(pedidoDTO.getNumeroProducto());
        pedido.setNombreProducto(pedidoDTO.getNombreProducto());
        pedido.setCantidad(pedidoDTO.getCantidad());
        pedido.setPrecioUnitario(pedidoDTO.getPrecioUnitario());
        pedido.setPrecioTotal(precioTotal);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDescripcion(pedidoDTO.getDescripcion());
        pedido.setDireccionEnvio(pedidoDTO.getDireccionEnvio());
        pedido.setFechaCreacion(LocalDateTime.now());

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        log.info("Pedido creado con ID: {}", pedidoGuardado.getId());

        return convertirADTO(pedidoGuardado);
    }

    /**
     * Actualizar estado del pedido
     */
    public PedidoDTO actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        pedido.setEstado(nuevoEstado);
        pedido.setFechaActualizacion(LocalDateTime.now());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Pedido actualizado. ID: {}, Nuevo estado: {}", id, nuevoEstado);

        return convertirADTO(pedidoActualizado);
    }

    /**
     * Cancelar pedido
     */
    public PedidoDTO cancelar(Long id) {
        return actualizarEstado(id, EstadoPedido.CANCELADO);
    }

    /**
     * Confirmar pedido
     */
    public PedidoDTO confirmar(Long id) {
        return actualizarEstado(id, EstadoPedido.CONFIRMADO);
    }

    /**
     * Obtener información completa del pedido con datos del usuario
     */
    public PedidoConDetallesDTO obtenerPedidoConDetalles(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        // COMUNICACIÓN INTER-SERVICIOS: Obtener información del usuario
        UsuarioDTO usuario = usuarioServiceClient.obtenerInfoBasicaUsuario(pedido.getUsuarioId());

        return new PedidoConDetallesDTO(
                convertirADTO(pedido),
                usuario
        );
    }

    // Métodos auxiliares
    private PedidoDTO convertirADTO(Pedido pedido) {
        return new PedidoDTO(
                pedido.getId(),
                pedido.getUsuarioId(),
                pedido.getNumeroProducto(),
                pedido.getNombreProducto(),
                pedido.getCantidad(),
                pedido.getPrecioUnitario(),
                pedido.getPrecioTotal(),
                pedido.getEstado(),
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion(),
                pedido.getDescripcion(),
                pedido.getDireccionEnvio()
        );
    }

    /**
     * DTO que contiene información del pedido junto con detalles del usuario
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PedidoConDetallesDTO {
        private PedidoDTO pedido;
        private UsuarioDTO usuario;
    }
}
