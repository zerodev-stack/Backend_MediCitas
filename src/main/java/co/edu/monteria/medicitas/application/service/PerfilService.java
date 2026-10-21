package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.PerfilUseCase;
import co.edu.monteria.medicitas.application.ports.out.UsuarioRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.DatosInvalidosException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio del perfil del usuario autenticado.
 *
 * El identificador del usuario siempre llega desde el token (lo resuelve el controlador con
 * ContextoUsuarioPort), nunca desde la petición: así nadie puede modificar la cuenta de otro.
 */
@Service
@Transactional
public class PerfilService implements PerfilUseCase {

    /** Mismas reglas que muestra la app: 8 caracteres, una mayúscula y un número. */
    private static final int LONGITUD_MINIMA = 8;

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilService(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPerfil(Long usuarioId) {
        return buscar(usuarioId);
    }

    @Override
    public Usuario actualizarContacto(Long usuarioId, String email, String telefono) {
        Usuario usuario = buscar(usuarioId);
        String emailNuevo = email.trim();

        boolean cambiaEmail = !emailNuevo.equalsIgnoreCase(usuario.getEmail());
        if (cambiaEmail && usuarioRepository.existePorEmail(emailNuevo)) {
            throw new DatosInvalidosException("Ese correo ya está registrado en otra cuenta.");
        }

        usuario.setEmail(emailNuevo);
        usuario.setTelefono(telefono == null || telefono.isBlank() ? null : telefono.trim());
        return usuarioRepository.guardar(usuario);
    }

    @Override
    public void cambiarContrasena(Long usuarioId, String contrasenaActual, String contrasenaNueva) {
        Usuario usuario = buscar(usuarioId);

        if (!passwordEncoder.matches(contrasenaActual, usuario.getPasswordHash())) {
            throw new DatosInvalidosException("La contraseña actual es incorrecta. Inténtalo de nuevo.");
        }
        if (contrasenaNueva.equals(contrasenaActual)) {
            throw new DatosInvalidosException("La nueva contraseña debe ser distinta de la actual.");
        }
        if (contrasenaNueva.length() < LONGITUD_MINIMA
                || contrasenaNueva.chars().noneMatch(Character::isUpperCase)
                || contrasenaNueva.chars().noneMatch(Character::isDigit)) {
            throw new DatosInvalidosException(
                    "La nueva contraseña debe tener mínimo 8 caracteres, una mayúscula y un número.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(contrasenaNueva));
        usuarioRepository.guardar(usuario);
    }

    private Usuario buscar(Long usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
