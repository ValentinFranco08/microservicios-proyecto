// DTO (Data Transfer Object) para representar os dados do usuário
package com.microservices.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String pais;
    private Boolean activo;
}
  
  