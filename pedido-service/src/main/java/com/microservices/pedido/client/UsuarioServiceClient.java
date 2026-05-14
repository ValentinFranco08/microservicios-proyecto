package com.microservices.pedido.client;

import com.microservices.pedido.dto.UsuarioDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceClient {

    private final RestTemplate restTemplate;

    @Value("${usuario.service.url:http://usuario-service:8001/usuario-service}")
    private String usuarioServiceUrl;

    /**
     * Obtener información del usuario desde el servicio de Usuario
     */
    public UsuarioDTO obtenerUsuario(Long usuarioId) {
        try {
            String url = usuarioServiceUrl + "/api/v1/usuarios/" + usuarioId;
            log.info("Llamando a servicio de Usuario: {}", url);
            
            UsuarioDTO usuario = restTemplate.getForObject(url, UsuarioDTO.class);
            log.info("Usuario obtenido: {}", usuario);
            
            return usuario;
        } catch (RestClientException e) {
            log.error("Error al obtener usuario con ID: {}", usuarioId, e);
            throw new RuntimeException("No se pudo conectar con el servicio de Usuario", e);
        }
    }

    /**
     * Verificar si un usuario existe
     */
    public boolean usuarioExiste(Long usuarioId) {
        try {
            String url = usuarioServiceUrl + "/api/v1/usuarios/" + usuarioId + "/existe";
            log.info("Verificando existencia de usuario: {}", url);
            
            Boolean existe = restTemplate.getForObject(url, Boolean.class);
            log.info("Usuario existe: {}", existe);
            
            return existe != null && existe;
        } catch (RestClientException e) {
            log.error("Error al verificar existencia de usuario con ID: {}", usuarioId, e);
            return false;
        }
    }

    /**
     * Obtener información básica del usuario
     */
    public UsuarioDTO obtenerInfoBasicaUsuario(Long usuarioId) {
        try {
            String url = usuarioServiceUrl + "/api/v1/usuarios/" + usuarioId + "/info-basica";
            log.info("Obteniendo información básica del usuario: {}", url);
            
            UsuarioDTO usuario = restTemplate.getForObject(url, UsuarioDTO.class);
            log.info("Información básica obtenida: {}", usuario);
            
            return usuario;
        } catch (RestClientException e) {
            log.error("Error al obtener información básica del usuario con ID: {}", usuarioId, e);
            throw new RuntimeException("No se pudo obtener información del usuario", e);
        }
    }
}
