package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Usuario;

/**
 * Respuesta del inicio de sesión.
 *
 * Incluye el usuario verificado, no un objeto fabricado en el cliente: la app móvil usa
 * el 'rol' que aquí llega para dirigir la navegación, de modo que ese dato debe proceder
 * del servidor y no de una heurística local.
 */
public record AuthResponse(String token, String tipoToken, UsuarioResponse usuario) {

    public static final String BEARER = "Bearer";

    public static AuthResponse de(Usuario usuario, String token) {
        return new AuthResponse(token, BEARER, UsuarioResponse.desde(usuario));
    }
}
