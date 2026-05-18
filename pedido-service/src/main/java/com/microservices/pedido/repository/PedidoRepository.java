// PedidoRepository.java - Repositorio para la entidad Pedido, con métodos personalizados para buscar por usuario y estado.
package com.microservices.pedido.repository;

import com.microservices.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByEstado(Pedido.EstadoPedido estado);

    List<Pedido> findByUsuarioIdAndEstado(Long usuarioId, Pedido.EstadoPedido estado);
}
