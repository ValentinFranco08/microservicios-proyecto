// PedidoController.java
package com.microservices.pedido.controller;

import com.microservices.pedido.dto.PedidoDTO;
import com.microservices.pedido.entity.Pedido.EstadoPedido;
import com.microservices.pedido.service.PedidoService;
import com.microservices.pedido.service.PedidoService.PedidoConDetallesDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestión de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    @Operation(summary = "Obtener todos los pedidos", description = "Retorna una lista de todos los pedidos registrados")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente")
    public ResponseEntity<List<PedidoDTO>> obtenerTodos() {
        return ResponseEntity.ok(pedidoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID", description = "Retorna los detalles de un pedido específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<PedidoDTO> obtenerPorId(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/detalles")
    @Operation(summary = "Obtener pedido con detalles del usuario", 
               description = "Retorna el pedido junto con información del usuario que lo realizó")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido con detalles encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<PedidoConDetallesDTO> obtenerConDetalles(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoConDetalles(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener pedidos por usuario", description = "Retorna todos los pedidos de un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos encontrados"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<List<PedidoDTO>> obtenerPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPorUsuario(usuarioId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener pedidos por estado", description = "Retorna todos los pedidos con un estado específico")
    @ApiResponse(responseCode = "200", description = "Pedidos encontrados")
    public ResponseEntity<List<PedidoDTO>> obtenerPorEstado(
            @Parameter(description = "Estado del pedido") @PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.obtenerPorEstado(estado));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo pedido", description = "Crea un nuevo pedido. Valida que el usuario exista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<PedidoDTO> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del nuevo pedido",
                    content = @Content(schema = @Schema(implementation = PedidoDTO.class))
            )
            @RequestBody PedidoDTO pedidoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(pedidoDTO));
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del pedido", description = "Actualiza el estado de un pedido existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<PedidoDTO> actualizarEstado(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "Nuevo estado del pedido") @RequestParam EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado));
    }

    @PutMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar pedido", description = "Cambia el estado del pedido a CONFIRMADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido confirmado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<PedidoDTO> confirmar(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmar(id));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pedido", description = "Cambia el estado del pedido a CANCELADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido cancelado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<PedidoDTO> cancelar(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }
}
