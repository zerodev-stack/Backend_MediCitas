package co.edu.monteria.medicitas.infrastructure.adapters.out.security;

import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.domain.model.UsuarioActual;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador que resuelve el principal autenticado desde el contexto de seguridad de Spring.
 *
 * Es la ÚNICA pieza que conoce el mecanismo de autenticación. La capa de aplicación recibe
 * un 'UsuarioActual' y nunca ve tokens, claims ni el 'SecurityContext'.
 */
@Component
public class SecurityContextContextoUsuarioAdapter implements ContextoUsuarioPort {

    @Override
    public UsuarioActual usuarioActual() {
        return principalAutenticado()
                .orElseThrow(() -> new AccessDeniedException("La solicitud no está autenticada."));
    }

    @Override
    public Optional<UsuarioActual> principalAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        if (!(auth.getPrincipal() instanceof UsuarioPrincipal principal) || principal.getUsuarioId() == null) {
            return Optional.empty();
        }

        return Optional.of(new UsuarioActual(
                principal.getUsuarioId(),
                principal.getUsername(),
                principal.getNombreCompleto(),
                principal.getNumDocumento(),
                principal.getRol()
        ));
    }
}
