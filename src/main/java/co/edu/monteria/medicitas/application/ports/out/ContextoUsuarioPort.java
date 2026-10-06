package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.UsuarioActual;

import java.util.Optional;

public interface ContextoUsuarioPort {

    /**
     * @return el principal autenticado.
     * @throws org.springframework.security.access.AccessDeniedException si la petición no
     *         está autenticada. Los adaptadores lanzan esa excepción; nunca devuelven null.
     */
    UsuarioActual usuarioActual();

    /**
     * Variante no lanzante, útil cuando la ausencia de principal es un caso válido
     * (por ejemplo, un endpoint público que personaliza su respuesta).
     */
    Optional<UsuarioActual> principalAutenticado();
}
