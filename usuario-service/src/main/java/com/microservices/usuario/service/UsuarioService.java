package com.microservices.usuario.service;

import com.microservices.usuario.dto.UsuarioDTO;
import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtener todos los usuarios
     */
    public List<UsuarioDTO> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener usuario por ID
     */
    public UsuarioDTO obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Obtener usuario por email
     */
    public UsuarioDTO obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }

    /**
     * Crear nuevo usuario
     */
    public UsuarioDTO crear(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + usuarioDTO.getEmail());
        }

        Usuario usuario = convertirAEntidad(usuarioDTO);
        usuario.setActivo(true);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertirADTO(usuarioGuardado);
    }

    /**
     * Actualizar usuario
     */
    public UsuarioDTO actualizar(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setDireccion(usuarioDTO.getDireccion());
        usuario.setCiudad(usuarioDTO.getCiudad());
        usuario.setPais(usuarioDTO.getPais());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertirADTO(usuarioActualizado);
    }

    /**
     * Eliminar usuario (borrado lógico)
     */
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Verificar si un usuario existe (usado por otros microservicios)
     */
    public boolean existeUsuario(Long id) {
        return usuarioRepository.existsById(id);
    }

    /**
     * Obtener información básica del usuario (para otros servicios)
     */
    public UsuarioDTO obtenerInfoBasica(Long id) {
        return obtenerPorId(id);
    }

    // Métodos auxiliares
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getCiudad(),
                usuario.getPais(),
                usuario.getActivo()
        );
    }

    private Usuario convertirAEntidad(UsuarioDTO usuarioDTO) {
        return new Usuario(
                usuarioDTO.getId(),
                usuarioDTO.getEmail(),
                usuarioDTO.getNombre(),
                usuarioDTO.getApellido(),
                usuarioDTO.getTelefono(),
                usuarioDTO.getDireccion(),
                usuarioDTO.getCiudad(),
                usuarioDTO.getPais(),
                usuarioDTO.getActivo()
        );
    }
}
