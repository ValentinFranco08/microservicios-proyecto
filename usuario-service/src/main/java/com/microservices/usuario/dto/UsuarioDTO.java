// DTO (Data Transfer Object) para representar os dados do usuário
package com.microservices.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String pais;

    public UsuarioDTO(Long id, String email, String nombre, String apellido, String telefono,
                      String direccion, String ciudad, String pais, Boolean activo) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.pais = pais;
        this.activo = activo;
    }

    public UsuarioDTO(Long id, String nombre, String apellido, String email, Boolean activo,
                      LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
}
  
  
