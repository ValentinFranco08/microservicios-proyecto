// PedidoDTO.java - DTO para transferir datos de pedidos entre capas, con campos relevantes para la presentación y lógica de negocio.
package com.microservices.pedido.dto;

import com.microservices.pedido.entity.Pedido.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private Long id;
    private Long usuarioId;
    private String numeroProducto;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotal;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String descripcion;
    private String direccionEnvio;
}
