package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Usuario;

public interface JwtPort {
    String generarToken(Usuario usuario);
    String obtenerEmailDesdeToken(String token);
    boolean validarToken(String token);
}
