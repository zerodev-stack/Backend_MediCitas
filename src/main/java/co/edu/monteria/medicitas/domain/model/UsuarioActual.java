package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.RolUsuario;

/**
 * Proyección inmutable del principal autenticado (portador del token JWT).
 *
 * Es el valor que la capa de aplicación recibe del adaptador de contexto de seguridad.
 * Al livingir en el dominio, los casos de uso pueden validar pertenencias sin
 * depender de Spring Security ni de la capa web.
 */
public class UsuarioActual {

    private final Long id;
    private final String email;
    private final String nombreCompleto;
    private final String numDocumento;
    private final RolUsuario rol;

    public UsuarioActual(Long id, String email, String nombreCompleto, String numDocumento, RolUsuario rol) {
        this.id = id;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.numDocumento = numDocumento;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNumDocumento() {
        return numDocumento;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public boolean es(RolUsuario rolEsperado) {
        return rol == rolEsperado;
    }

    public boolean esMedico() {
        return es(RolUsuario.ROLE_MEDICO);
    }

    public boolean esAdmin() {
        return es(RolUsuario.ROLE_ADMIN);
    }
}
