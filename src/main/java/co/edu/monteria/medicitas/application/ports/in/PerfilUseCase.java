package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.Usuario;

/**
 * Casos de uso del perfil del usuario autenticado ("Mi perfil" en la app).
 *
 * El nombre y el documento no se pueden cambiar: vienen de la afiliación BDUA. Solo se
 * permiten los datos de contacto y la contraseña.
 */
public interface PerfilUseCase {

    Usuario obtenerPerfil(Long usuarioId);

    /**
     * Actualiza correo y teléfono. Lanza DatosInvalidosException si el correo ya pertenece
     * a otra cuenta.
     */
    Usuario actualizarContacto(Long usuarioId, String email, String telefono);

    /**
     * Cambia la contraseña tras verificar la actual. Lanza DatosInvalidosException si la
     * actual no coincide o si la nueva no cumple los requisitos.
     */
    void cambiarContrasena(Long usuarioId, String contrasenaActual, String contrasenaNueva);
}
