package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Eps;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.RolUsuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;

import java.time.LocalDateTime;

/**
 * Respuesta pública de un usuario.
 *
 * POR QUÉ EXISTE: los controladores ya no serializan el modelo de dominio 'Usuario'. Al
 * definir aquí la forma de la respuesta, el hash de la contraseña queda excluido por
 * construcción y no por una configuración de Jackson que pueda olvidarse.
 */
public record UsuarioResponse(
        Long id,
        String numDocumento,
        TipoDocumento tipoDocumento,
        String nombreCompleto,
        String email,
        RolUsuario rol,
        String telefono,
        Eps eps,
        LocalDateTime fechaRegistro
) {

    public static UsuarioResponse desde(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNumDocumento(),
                usuario.getTipoDocumento(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getTelefono(),
                usuario.getEps(),
                usuario.getFechaRegistro()
        );
    }
}
